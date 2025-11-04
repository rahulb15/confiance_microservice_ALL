package com.microservices.api_gateway.config;

import com.microservices.common.core.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Bean
    public JwtUtil jwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration:86400000}") long jwtExpiration,
            @Value("${jwt.refresh-expiration:604800000}") long refreshTokenExpiration) {
        return new JwtUtil(secret, jwtExpiration, refreshTokenExpiration);
    }
}