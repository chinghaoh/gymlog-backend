package com.gymlog.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    //TODO Change to redis cache
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createBucket(int capacity, Duration duration) {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(capacity, duration));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket getBucket(String key, int capacity, Duration duration) {
        return buckets.computeIfAbsent(key, k -> createBucket(capacity, duration));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();

        // check specific endpoint limits
        Integer capacity = null;
        Duration duration = Duration.ofMinutes(1);

        switch (uri) {
            case "/api/auth/login"           -> { capacity = 5;  duration = Duration.ofMinutes(1); }
            case "/api/auth/register"        -> { capacity = 3;  duration = Duration.ofMinutes(1); }
            case "/api/auth/forgot-password" -> { capacity = 3;  duration = Duration.ofMinutes(10); }
            case "/api/auth/reset-password"  -> { capacity = 5;  duration = Duration.ofMinutes(10); }
            case "/api/auth/verify"          -> { capacity = 5;  duration = Duration.ofMinutes(10); }
            case "/api/ai/chat"              -> { capacity = 10; duration = Duration.ofHours(1); }
            default -> {
                if (uri.startsWith("/api/")) {
                    capacity = 100;
                    duration = Duration.ofMinutes(1);
                }
            }
        }

        if (capacity != null) {
            String key = ip + ":" + uri;
            Bucket bucket = getBucket(key, capacity, duration);

            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"message\": \"Too many requests. Please try again later.\", \"status\": 429}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}