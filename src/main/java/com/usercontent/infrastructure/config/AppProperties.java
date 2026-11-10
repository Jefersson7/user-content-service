package com.usercontent.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties(prefix = "app")
public record AppProperties(External external, Cache cache) {

    public record External(
            @DefaultValue("https://jsonplaceholder.typicode.com") URI baseUrl,
            @DefaultValue("2s") Duration connectTimeout,
            @DefaultValue("3s") Duration readTimeout) {
    }

    public record Cache(@DefaultValue("5m") Duration ttl) {
    }
}
