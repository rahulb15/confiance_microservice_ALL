package com.microservices.api_gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.common.core.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingFilter implements GatewayFilter {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final int DEFAULT_REQUESTS_PER_MINUTE = 60;
    private static final int AUTH_REQUESTS_PER_MINUTE = 10; // Lower limit for auth endpoints

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String clientId = getClientId(request);
        String path = request.getURI().getPath();

        int requestLimit = getRequestLimit(path);
        String key = "rate_limit:" + clientId + ":" + getTimeWindow();

        return redisTemplate.opsForValue()
                .increment(key)
                .flatMap(currentCount -> {
                    if (currentCount == 1) {
                        // Set expiration for the key
                        return redisTemplate.expire(key, Duration.ofMinutes(1))
                                .then(processRequest(exchange, chain, currentCount, requestLimit));
                    } else {
                        return processRequest(exchange, chain, currentCount, requestLimit);
                    }
                })
                .onErrorResume(throwable -> {
                    log.error("Error in rate limiting", throwable);
                    // Continue without rate limiting on Redis error
                    return chain.filter(exchange);
                });
    }

    private Mono<Void> processRequest(ServerWebExchange exchange, GatewayFilterChain chain,
                                      Long currentCount, int requestLimit) {

        ServerHttpResponse response = exchange.getResponse();

        // Add rate limit headers
        response.getHeaders().add("X-RateLimit-Limit", String.valueOf(requestLimit));
        response.getHeaders().add("X-RateLimit-Remaining",
                String.valueOf(Math.max(0, requestLimit - currentCount)));
        response.getHeaders().add("X-RateLimit-Reset", String.valueOf(getNextResetTime()));

        if (currentCount > requestLimit) {
            log.warn("Rate limit exceeded for client: {}, current count: {}, limit: {}",
                    getClientId(exchange.getRequest()), currentCount, requestLimit);
            return handleRateLimitExceeded(exchange);
        }

        return chain.filter(exchange);
    }

    private String getClientId(ServerHttpRequest request) {
        // Try to get client ID from various sources
        String clientId = request.getHeaders().getFirst("X-Client-ID");
        if (clientId != null) return clientId;

        String userAgent = request.getHeaders().getFirst("User-Agent");
        if (userAgent != null) return userAgent.hashCode() + "";

        String remoteAddr = request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";

        return remoteAddr;
    }

    private int getRequestLimit(String path) {
        if (path.startsWith("/auth/")) {
            return AUTH_REQUESTS_PER_MINUTE;
        }
        return DEFAULT_REQUESTS_PER_MINUTE;
    }

    private long getTimeWindow() {
        // Current minute timestamp
        return System.currentTimeMillis() / 60000;
    }

    private long getNextResetTime() {
        // Next minute timestamp
        return ((System.currentTimeMillis() / 60000) + 1) * 60;
    }

    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            ApiResponse<Object> errorResponse = ApiResponse.error(
                    "Too many requests. Please try again later.",
                    HttpStatus.TOO_MANY_REQUESTS.value()
            );

            byte[] bytes = objectMapper.writeValueAsString(errorResponse).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));

        } catch (Exception e) {
            log.error("Error writing rate limit error response", e);
            return response.setComplete();
        }
    }
}
