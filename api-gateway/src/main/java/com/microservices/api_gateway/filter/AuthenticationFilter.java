package com.microservices.api_gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.common.core.dto.ApiResponse;
import com.microservices.common.core.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GatewayFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    // Public endpoints that don't require authentication
    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/auth/login",
            "/auth/register",
            "/auth/refresh",
            "/swagger-ui",
            "/v3/api-docs",
            "/webjars",
            "/actuator/health"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        log.debug("Processing authentication for path: {}", path);

        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            log.debug("Public endpoint accessed: {}", path);
            return chain.filter(exchange);
        }

        // Extract JWT token
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            log.warn("Missing Authorization header for path: {}", path);
            return handleAuthenticationError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
        }

        try {
            // Validate token
            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid token for path: {}", path);
                return handleAuthenticationError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            // Extract user information from token
            String username = jwtUtil.extractUsername(token);
            Set<String> roles = jwtUtil.extractRoles(token);

            log.debug("Authenticated user: {} with roles: {} for path: {}", username, roles, path);

            // Check role-based access for admin endpoints
            if (isAdminEndpoint(path) && !hasAdminRole(roles)) {
                log.warn("Access denied for user {} to admin endpoint: {}", username, path);
                return handleAuthenticationError(exchange, "Access denied", HttpStatus.FORBIDDEN);
            }

            // Add user information to headers for downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", username)
                    .header("X-User-Roles", String.join(",", roles))
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("Authentication error for path: {}, error: {}", path, e.getMessage());
            return handleAuthenticationError(exchange, "Authentication failed", HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private boolean isAdminEndpoint(String path) {
        return path.startsWith("/eureka") ||
                path.startsWith("/config") ||
                (path.startsWith("/users") && (path.contains("/admin") ||
                        path.matches(".*/users/\\d+/(activate|deactivate|roles).*")));
    }

    private boolean hasAdminRole(Set<String> roles) {
        return roles.contains("ADMIN");
    }

    private String extractToken(ServerHttpRequest request) {
        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }
        return null;
    }

    private Mono<Void> handleAuthenticationError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            ApiResponse<Object> errorResponse = ApiResponse.error(message, status.value());
            byte[] bytes = objectMapper.writeValueAsString(errorResponse).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("Error writing authentication error response", e);
            return response.setComplete();
        }
    }
}
