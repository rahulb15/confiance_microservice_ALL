package com.microservices.api_gateway.config;

import com.microservices.api_gateway.filter.AuthenticationFilter;
import com.microservices.api_gateway.filter.LoggingFilter;
import com.microservices.api_gateway.filter.RateLimitingFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final AuthenticationFilter authenticationFilter;
    private final LoggingFilter loggingFilter;
    private final RateLimitingFilter rateLimitingFilter;

    public GatewayConfig(AuthenticationFilter authenticationFilter,
                         LoggingFilter loggingFilter,
                         RateLimitingFilter rateLimitingFilter) {
        this.authenticationFilter = authenticationFilter;
        this.loggingFilter = loggingFilter;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service Routes (Public)
                .route("auth-service-public", r -> r
                        .path("/auth/login", "/auth/register", "/auth/refresh")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(rateLimitingFilter)
                                .circuitBreaker(config -> config
                                        .setName("auth-service-cb")
                                        .setFallbackUri("forward:/fallback/auth")))
                        .uri("lb://auth-service"))

                // Auth Service Routes (Protected)
                .route("auth-service-protected", r -> r
                        .path("/auth/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(authenticationFilter)
                                .filter(rateLimitingFilter)
                                .circuitBreaker(config -> config
                                        .setName("auth-service-cb")
                                        .setFallbackUri("forward:/fallback/auth")))
                        .uri("lb://auth-service"))

                // User Service Routes (Protected)
                .route("user-service", r -> r
                        .path("/users/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(authenticationFilter)
                                .filter(rateLimitingFilter)
                                .circuitBreaker(config -> config
                                        .setName("user-service-cb")
                                        .setFallbackUri("forward:/fallback/users")))
                        .uri("lb://user-service"))

                // Eureka Server (Admin only)
                .route("eureka-server", r -> r
                        .path("/eureka/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(authenticationFilter)
                                .rewritePath("/eureka/(?<segment>.*)", "/${segment}"))
                        .uri("lb://eureka-server"))

                // Config Server (Admin only)
                .route("config-server", r -> r
                        .path("/config/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(authenticationFilter)
                                .rewritePath("/config/(?<segment>.*)", "/${segment}"))
                        .uri("lb://config-server"))

                // API Documentation Routes (Public)
                .route("swagger-ui", r -> r
                        .path("/swagger-ui/**", "/v3/api-docs/**", "/webjars/**")
                        .filters(f -> f.filter(loggingFilter))
                        .uri("lb://api-gateway"))

                .build();
    }
}