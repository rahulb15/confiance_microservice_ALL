package com.microservices.common.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal {
    private Long id;
    private String username;
    private String email;
    private Set<String> roles;
    private boolean enabled;
    private LocalDateTime lastLoginAt;
}
