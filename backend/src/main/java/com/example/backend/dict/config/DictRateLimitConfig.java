package com.example.backend.dict.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class DictRateLimitConfig {

    @Bean(name = "dictRateLimitBucket")
    public Bucket dictRateLimitBucket(DictClientProperties properties) {
        DictClientProperties.RateLimit rl = properties.getRateLimit();
        Duration refillDuration = DurationStyle.SIMPLE.parse(rl.getRefillDuration());
        Bandwidth limit = Bandwidth.classic(rl.getCapacity(), Refill.greedy(rl.getRefillTokens(), refillDuration));
        return Bucket.builder().addLimit(limit).build();
    }
}
