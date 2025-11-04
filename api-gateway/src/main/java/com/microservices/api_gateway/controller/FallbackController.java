package com.microservices.api_gateway.controller;

import com.microservices.common.core.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public ResponseEntity<ApiResponse<String>> authServiceFallback() {
        log.warn("Auth service fallback triggered");

        ApiResponse<String> response = ApiResponse.error(
                "Authentication service is temporarily unavailable. Please try again later.",
                HttpStatus.SERVICE_UNAVAILABLE.value()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<String>> userServiceFallback() {
        log.warn("User service fallback triggered");

        ApiResponse<String> response = ApiResponse.error(
                "User service is temporarily unavailable. Please try again later.",
                HttpStatus.SERVICE_UNAVAILABLE.value()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/generic")
    public ResponseEntity<ApiResponse<String>> genericFallback() {
        log.warn("Generic fallback triggered");

        ApiResponse<String> response = ApiResponse.error(
                "Service is temporarily unavailable. Please try again later.",
                HttpStatus.SERVICE_UNAVAILABLE.value()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
