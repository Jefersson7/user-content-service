package com.usercontent.infrastructure.adapter.out.client;

import com.usercontent.domain.exception.ExternalApiException;
import com.usercontent.domain.exception.UserNotFoundException;
import com.usercontent.domain.model.Post;
import com.usercontent.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ConfigBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class JsonPlaceholderClientTest {

    private MockRestServiceServer server;
    private JsonPlaceholderClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://jsonplaceholder.typicode.com");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new JsonPlaceholderClient(builder.build(), passthroughCircuitBreakerFactory());
    }

    @Test
    void mapsUserResponse() {
        server.expect(requestTo("https://jsonplaceholder.typicode.com/users/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"id":1,"name":"Leanne Graham","username":"Bret","email":"Sincere@april.biz"}
                        """, MediaType.APPLICATION_JSON));

        User user = client.fetchUser(1);

        assertThat(user).isEqualTo(new User(1L, "Leanne Graham", "Bret", "Sincere@april.biz"));
        server.verify();
    }

    @Test
    void mapsPostsResponse() {
        server.expect(requestTo("https://jsonplaceholder.typicode.com/posts?userId=1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [{"userId":1,"id":1,"title":"title","body":"body"}]
                        """, MediaType.APPLICATION_JSON));

        List<Post> posts = client.fetchPostsByUser(1);

        assertThat(posts).containsExactly(new Post(1L, 1L, "title", "body"));
        server.verify();
    }

    @Test
    void translatesNotFoundIntoDomainException() {
        server.expect(requestTo("https://jsonplaceholder.typicode.com/users/99"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.fetchUser(99))
                .isInstanceOf(UserNotFoundException.class);
        server.verify();
    }

    @Test
    void translatesProviderFailureIntoExternalApiException() {
        server.expect(requestTo("https://jsonplaceholder.typicode.com/users/1"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.fetchUser(1))
                .isInstanceOf(ExternalApiException.class);
        server.verify();
    }

    private static CircuitBreakerFactory<?, ?> passthroughCircuitBreakerFactory() {
        return new CircuitBreakerFactory<Object, ConfigBuilder<Object>>() {
            @Override
            public CircuitBreaker create(String id) {
                return new CircuitBreaker() {
                    @Override
                    public <T> T run(Supplier<T> toRun, Function<Throwable, T> fallback) {
                        try {
                            return toRun.get();
                        } catch (Throwable throwable) {
                            return fallback.apply(throwable);
                        }
                    }
                };
            }

            @Override
            protected ConfigBuilder<Object> configBuilder(String id) {
                return null;
            }

            @Override
            public void configureDefault(Function<String, Object> defaultConfiguration) {
            }
        };
    }
}
