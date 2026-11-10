package com.usercontent.infrastructure.adapter.out.client;

import com.usercontent.domain.exception.ExternalApiException;
import com.usercontent.domain.exception.UserNotFoundException;
import com.usercontent.domain.model.Comment;
import com.usercontent.domain.model.Post;
import com.usercontent.domain.model.User;
import com.usercontent.domain.port.out.ExternalContentPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Component
public class JsonPlaceholderClient implements ExternalContentPort {

    private static final Logger log = LoggerFactory.getLogger(JsonPlaceholderClient.class);
    private static final String CIRCUIT_BREAKER = "jsonplaceholder";

    private final RestClient restClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public JsonPlaceholderClient(RestClient jsonPlaceholderRestClient,
                                 CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.restClient = jsonPlaceholderRestClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @Override
    public User fetchUser(long userId) {
        JsonUser response = call("GET /users/" + userId, () -> {
            try {
                return restClient.get().uri("/users/{id}", userId).retrieve().body(JsonUser.class);
            } catch (HttpClientErrorException.NotFound ex) {
                throw new UserNotFoundException(userId);
            }
        });

        if (response == null) {
            throw new UserNotFoundException(userId);
        }
        return new User(response.id(), response.name(), response.username(), response.email());
    }

    @Override
    public List<Post> fetchPostsByUser(long userId) {
        JsonPost[] response = call("GET /posts?userId=" + userId,
                () -> restClient.get().uri("/posts?userId={id}", userId).retrieve().body(JsonPost[].class));

        return Arrays.stream(response == null ? new JsonPost[0] : response)
                .map(post -> new Post(post.id(), post.userId(), post.title(), post.body()))
                .toList();
    }

    @Override
    public List<Comment> fetchCommentsByPost(long postId) {
        JsonComment[] response = call("GET /comments?postId=" + postId,
                () -> restClient.get().uri("/comments?postId={id}", postId).retrieve().body(JsonComment[].class));

        return Arrays.stream(response == null ? new JsonComment[0] : response)
                .map(comment -> new Comment(comment.id(), comment.postId(), comment.name(), comment.email(), comment.body()))
                .toList();
    }

    private <T> T call(String operation, Supplier<T> supplier) {
        long start = System.nanoTime();
        try {
            return circuitBreakerFactory.create(CIRCUIT_BREAKER).run(supplier, throwable -> {
                if (throwable instanceof UserNotFoundException notFound) {
                    throw notFound;
                }
                log.warn("{} failed: {}", operation, throwable.toString());
                throw new ExternalApiException(operation + " failed", throwable);
            });
        } finally {
            log.debug("{} took {} ms", operation, (System.nanoTime() - start) / 1_000_000);
        }
    }

    record JsonUser(Long id, String name, String username, String email) {
    }

    record JsonPost(Long userId, Long id, String title, String body) {
    }

    record JsonComment(Long postId, Long id, String name, String email, String body) {
    }
}
