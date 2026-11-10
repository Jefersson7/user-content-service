package com.usercontent.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableCaching
public class InfrastructureConfig {

    public static final String USER_SUMMARY_CACHE = "userSummary";
    public static final String TOP_POSTS_CACHE = "topPosts";

    @Bean
    RestClient jsonPlaceholderRestClient(AppProperties properties) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.external().connectTimeout());
        requestFactory.setReadTimeout(properties.external().readTimeout());

        return RestClient.builder()
                .baseUrl(properties.external().baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    CacheManager cacheManager(AppProperties properties) {
        var cacheManager = new CaffeineCacheManager(USER_SUMMARY_CACHE, TOP_POSTS_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(properties.cache().ttl())
                .maximumSize(1_000)
                .recordStats());
        return cacheManager;
    }
}
