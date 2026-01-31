package com.urlshortener.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitConfig {

    @Value("${rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${rate-limit.requests-per-hour:1000}")
    private int requestsPerHour;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, this::createBucket);
    }

    private Bucket createBucket(String key) {
        Bandwidth minuteLimit = Bandwidth.classic(
                requestsPerMinute,
                Refill.greedy(requestsPerMinute, Duration.ofMinutes(1)));

        Bandwidth hourLimit = Bandwidth.classic(
                requestsPerHour,
                Refill.greedy(requestsPerHour, Duration.ofHours(1)));

        return Bucket.builder()
                .addLimit(minuteLimit)
                .addLimit(hourLimit)
                .build();
    }

    public Bucket createStrictBucket() {
        Bandwidth limit = Bandwidth.classic(
                10,
                Refill.greedy(10, Duration.ofMinutes(1)));

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public Bucket createRedirectBucket() {
        Bandwidth limit = Bandwidth.classic(
                100,
                Refill.greedy(100, Duration.ofSeconds(10)));

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
