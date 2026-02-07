package com.example.backend.dict.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class DictRestTemplateConfig {

    @Bean(name = "dictRestTemplate")
    public RestTemplate dictRestTemplate(DictClientProperties properties) {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(properties.getRest().getConnectTimeout()))
                .setReadTimeout(Duration.ofMillis(properties.getRest().getReadTimeout()))
                .build();
    }
}
