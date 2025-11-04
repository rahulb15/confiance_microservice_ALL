package com.microservices.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
public class LoggingFilter implements GatewayFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        // Add trace ID to request headers
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Trace-ID", traceId)
                .build();

        long startTime = System.currentTimeMillis();

        log.info("Gateway Request - TraceID: {}, Method: {}, Path: {}, RemoteAddr: {}, UserAgent: {}, Timestamp: {}",
                traceId,
                request.getMethod(),
                request.getURI().getPath(),
                request.getRemoteAddress(),
                request.getHeaders().getFirst("User-Agent"),
                LocalDateTime.now());

        return chain.filter(exchange.mutate().request(modifiedRequest).build())
                .doFinally(signalType -> {
                    long duration = System.currentTimeMillis() - startTime;
                    int statusCode = exchange.getResponse().getStatusCode() != null ?
                            exchange.getResponse().getStatusCode().value() : 0;

                    log.info("Gateway Response - TraceID: {}, Status: {}, Duration: {}ms, Path: {}",
                            traceId, statusCode, duration, request.getURI().getPath());
                });
    }
}
