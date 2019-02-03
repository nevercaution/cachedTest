package com.nevercaution.cached_test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Bean
    public RedisDB getRedis() {
        return new RedisDB();
    }
}
