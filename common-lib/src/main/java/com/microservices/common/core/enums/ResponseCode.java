package com.microservices.common.core.enums;

import lombok.Getter;

@Getter
public enum ResponseCode {

    // Success Codes (2xx)
    SUCCESS(200, "Success"),
    CREATED(201, "Resource created successfully"),
    NO_CONTENT(204, "No content"),

    // Client Error Codes (4xx)
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Unauthorized access"),
    FORBIDDEN(403, "Access forbidden"),
    NOT_FOUND(404, "Resource not found"),
    METHOD_NOT_ALLOWED(405, "Method not allowed"),
    CONFLICT(409, "Resource conflict"),
    VALIDATION_FAILED(422, "Validation failed"),
    TOO_MANY_REQUESTS(429, "Too many requests"),

    // Server Error Codes (5xx)
    INTERNAL_SERVER_ERROR(500, "Internal server error"),
    SERVICE_UNAVAILABLE(503, "Service unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway timeout"),

    // Authentication & Authorization Codes
    INVALID_TOKEN(4001, "Invalid token"),
    EXPIRED_TOKEN(4002, "Token expired"),
    MISSING_TOKEN(4003, "Missing token"),
    INVALID_CREDENTIALS(4004, "Invalid credentials"),
    ACCOUNT_LOCKED(4005, "Account is locked"),
    ACCOUNT_DISABLED(4006, "Account is disabled"),

    // User Management Codes
    USER_NOT_FOUND(4101, "User not found"),
    USER_ALREADY_EXISTS(4102, "User already exists"),
    INVALID_USER_DATA(4103, "Invalid user data"),
    EMAIL_ALREADY_EXISTS(4104, "Email already exists"),
    USERNAME_ALREADY_EXISTS(4105, "Username already exists"),

    // Business Logic Codes
    INSUFFICIENT_PERMISSIONS(4201, "Insufficient permissions"),
    OPERATION_NOT_ALLOWED(4202, "Operation not allowed"),
    RESOURCE_LIMIT_EXCEEDED(4203, "Resource limit exceeded"),

    // Database Codes
    DATABASE_ERROR(5001, "Database error"),
    DATA_INTEGRITY_VIOLATION(5002, "Data integrity violation"),
    CONNECTION_ERROR(5003, "Database connection error");

    private final int code;
    private final String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
