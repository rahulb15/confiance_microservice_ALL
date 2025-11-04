package com.microservices.user.service;

import com.microservices.common.core.dto.ApiResponse;
import com.microservices.common.core.dto.UserPrincipal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", path = "/auth")
public interface AuthServiceClient {

    @GetMapping("/validate")
    ResponseEntity<ApiResponse<UserPrincipal>> validateToken(@RequestHeader("Authorization") String token);
}
