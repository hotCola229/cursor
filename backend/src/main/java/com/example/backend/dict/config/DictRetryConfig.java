package com.example.backend.dict.config;

import com.example.backend.dict.service.DictRetryableException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Collections;

@Configuration
public class DictRetryConfig {

    @Bean(name = "dictRetryTemplate")
    public RetryTemplate dictRetryTemplate(DictClientProperties properties) {
        RetryTemplate template = new RetryTemplate();
        SimpleRetryPolicy policy = new SimpleRetryPolicy(properties.getRetry().getMaxAttempts(),
                Collections.singletonMap(DictRetryableException.class, true));
        template.setRetryPolicy(policy);
        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(properties.getRetry().getInitialDelay());
        backOff.setMultiplier(properties.getRetry().getMultiplier());
        backOff.setMaxInterval(properties.getRetry().getMaxDelay());
        template.setBackOffPolicy(backOff);
        return template;
    }
}
