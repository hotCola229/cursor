package com.example.backend.dict.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "dict")
public class DictClientProperties {

    private Client client = new Client();
    private Rest rest = new Rest();
    private Retry retry = new Retry();
    private RateLimit rateLimit = new RateLimit();

    public static class Client {
        private String baseUrl = "http://172.20.4.32:18022/";
        private String appKey = "28dc15bb-2e2c-45e4-b435-525853f69173";
        private String appSecret = "0e27fdf2820802cdea8e0eb22b695c93";

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getAppKey() { return appKey; }
        public void setAppKey(String appKey) { this.appKey = appKey; }
        public String getAppSecret() { return appSecret; }
        public void setAppSecret(String appSecret) { this.appSecret = appSecret; }
    }

    public static class Rest {
        private int connectTimeout = 3000;
        private int readTimeout = 5000;

        public int getConnectTimeout() { return connectTimeout; }
        public void setConnectTimeout(int connectTimeout) { this.connectTimeout = connectTimeout; }
        public int getReadTimeout() { return readTimeout; }
        public void setReadTimeout(int readTimeout) { this.readTimeout = readTimeout; }
    }

    public static class Retry {
        private int maxAttempts = 3;
        private long initialDelay = 100;
        private double multiplier = 2.0;
        private long maxDelay = 2000;

        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
        public long getInitialDelay() { return initialDelay; }
        public void setInitialDelay(long initialDelay) { this.initialDelay = initialDelay; }
        public double getMultiplier() { return multiplier; }
        public void setMultiplier(double multiplier) { this.multiplier = multiplier; }
        public long getMaxDelay() { return maxDelay; }
        public void setMaxDelay(long maxDelay) { this.maxDelay = maxDelay; }
    }

    public static class RateLimit {
        private long capacity = 100;
        private long refillTokens = 100;
        private String refillDuration = "1s";

        public long getCapacity() { return capacity; }
        public void setCapacity(long capacity) { this.capacity = capacity; }
        public long getRefillTokens() { return refillTokens; }
        public void setRefillTokens(long refillTokens) { this.refillTokens = refillTokens; }
        public String getRefillDuration() { return refillDuration; }
        public void setRefillDuration(String refillDuration) { this.refillDuration = refillDuration; }
    }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public Rest getRest() { return rest; }
    public void setRest(Rest rest) { this.rest = rest; }
    public Retry getRetry() { return retry; }
    public void setRetry(Retry retry) { this.retry = retry; }
    public RateLimit getRateLimit() { return rateLimit; }
    public void setRateLimit(RateLimit rateLimit) { this.rateLimit = rateLimit; }
}
