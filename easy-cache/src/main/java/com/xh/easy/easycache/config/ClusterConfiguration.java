package com.xh.easy.easycache.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
public class ClusterConfiguration {

    public Set<String> getClusterIds() {
        return new HashSet<>();
    }

    @Bean
    public StatefulRedisConnection<String, String> assConfigRedisConnection(ConfigurableApplicationContext context) {
        return RedisClient.create("redis://password@localhost:6379").connect();
    }
}
