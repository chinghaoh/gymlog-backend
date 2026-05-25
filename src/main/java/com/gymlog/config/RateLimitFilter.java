package com.gymlog.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.redisson.Redisson;
import lombok.extern.slf4j.Slf4j;


import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedissonClient redissonClient;

    private ProxyManager<String> getProxyManager() {
        log.debug("Creating ProxyManager with Redisson client: {}", redissonClient);
        return RedissonBasedProxyManager.builderFor(
                ((Redisson) redissonClient).getCommandExecutor()
        ).build();
    }

    private BucketConfiguration buildConfig(int capacity, Duration duration) {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(capacity, duration));
        return BucketConfiguration.builder()
                .addLimit(limit)
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();

        Integer capacity = null;
        Duration duration = Duration.ofMinutes(1);

        switch (uri) {
            case "/api/auth/login"           -> { capacity = 5;   duration = Duration.ofMinutes(1); }
            case "/api/auth/register"        -> { capacity = 3;   duration = Duration.ofMinutes(1); }
            case "/api/auth/forgot-password" -> { capacity = 3;   duration = Duration.ofMinutes(10); }
            case "/api/auth/reset-password"  -> { capacity = 5;   duration = Duration.ofMinutes(10); }
            case "/api/auth/verify"          -> { capacity = 5;   duration = Duration.ofMinutes(10); }
            case "/api/auth/demo"            -> { capacity = 3;   duration = Duration.ofMinutes(10); }
            case "/api/ai/chat"              -> { capacity = 10;  duration = Duration.ofHours(1); }
            default -> {
                if (uri.startsWith("/api/")) {
                    capacity = 100;
                    duration = Duration.ofMinutes(1);
                }
            }
        }

        if (capacity != null) {
            String key = ip + ":" + uri;
            log.debug("Rate limiting key: {}", key);
            final int finalCapacity = capacity;
            final Duration finalDuration = duration;

            Supplier<BucketConfiguration> configSupplier = () ->
                    buildConfig(finalCapacity, finalDuration);

            var bucket = getProxyManager().builder()
                    .build(key, configSupplier);

            if (!bucket.tryConsume(1)) {
                log.debug("Rate limit exceeded for key: {}", key);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"message\": \"Too many requests. Please try again later.\", \"status\": 429}"
                );
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}