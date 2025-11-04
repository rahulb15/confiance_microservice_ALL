package com.microservices.common.core.exception;

import com.microservices.common.core.dto.ApiResponse;
import com.microservices.common.core.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Object>> handleBaseException(
            BaseException ex, HttpServletRequest request) {
        log.error("BaseException: {}", ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                ex.getResponseCode().getCode(),
                request.getRequestURI()
        );

        HttpStatus status = mapToHttpStatus(ex.getResponseCode().getCode());
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        log.error("ValidationException: {}", ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                ResponseCode.VALIDATION_FAILED.getCode(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("MethodArgumentNotValidException: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .error("Validation failed")
                .data(errors)
                .statusCode(ResponseCode.VALIDATION_FAILED.getCode())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        log.error("ConstraintViolationException: {}", ex.getMessage());

        String errors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        ApiResponse<Object> response = ApiResponse.error(
                "Constraint violation: " + errors,
                ResponseCode.VALIDATION_FAILED.getCode(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        log.error("AuthenticationException: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "Authentication failed: " + ex.getMessage(),
                ResponseCode.UNAUTHORIZED.getCode(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {
        log.error("BadCredentialsException: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "Invalid credentials",
                ResponseCode.INVALID_CREDENTIALS.getCode(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        log.error("AccessDeniedException: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "Access denied: " + ex.getMessage(),
                ResponseCode.FORBIDDEN.getCode(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.error("ResourceNotFoundException: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                ResponseCode.NOT_FOUND.getCode(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpServletRequest request) {
        log.error("NoResourceFoundException: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "Resource not found: " + ex.getMessage(),
                ResponseCode.NOT_FOUND.getCode(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.error("HttpRequestMethodNotSupportedException: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "HTTP method not supported: " + ex.getMethod(),
                ResponseCode.METHOD_NOT_ALLOWED.getCode(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.error("HttpMessageNotReadableException: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "Malformed JSON request",
                ResponseCode.BAD_REQUEST.getCode(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.error("MissingServletRequestParameterException: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "Missing request parameter: " + ex.getParameterName(),
                ResponseCode.BAD_REQUEST.getCode(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.error("MethodArgumentTypeMismatchException: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "Invalid parameter type: " + ex.getName(),
                ResponseCode.BAD_REQUEST.getCode(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.error(
                "An unexpected error occurred",
                ResponseCode.INTERNAL_SERVER_ERROR.getCode(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private HttpStatus mapToHttpStatus(int code) {
        return switch (code / 100) {
            case 2 -> HttpStatus.OK;
            case 4 -> switch (code) {
                case 400, 4001, 4002, 4003, 4103, 4203 -> HttpStatus.BAD_REQUEST;
                case 401, 4004, 4005, 4006 -> HttpStatus.UNAUTHORIZED;
                case 403, 4201, 4202 -> HttpStatus.FORBIDDEN;
                case 404, 4101 -> HttpStatus.NOT_FOUND;
                case 405 -> HttpStatus.METHOD_NOT_ALLOWED;
                case 409, 4102, 4104, 4105 -> HttpStatus.CONFLICT;
                case 422 -> HttpStatus.UNPROCESSABLE_ENTITY;
                case 429 -> HttpStatus.TOO_MANY_REQUESTS;
                default -> HttpStatus.BAD_REQUEST;
            };
            case 5 -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
