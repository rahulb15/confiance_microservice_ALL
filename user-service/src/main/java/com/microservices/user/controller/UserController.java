package com.microservices.user.controller;

import com.microservices.common.core.dto.ApiResponse;
import com.microservices.common.core.dto.UserPrincipal;
import com.microservices.user.dto.*;
import com.microservices.user.entity.User;
import com.microservices.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User CRUD operations and management")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Username or email already exists"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Validation error")
    })
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest request) {
        log.info("Create user request received for username: {}", request.getUsername());

        UserResponse user = userService.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", user));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve user information by ID")
    @PreAuthorize("hasRole('ADMIN') or authentication.name == @userService.getUserById(#id).username")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        log.debug("Get user request received for ID: {}", id);

        UserResponse user = userService.getUserById(id);

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username", description = "Retrieve user information by username")
    @PreAuthorize("hasRole('ADMIN') or authentication.name == #username")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(@PathVariable String username) {
        log.debug("Get user request received for username: {}", username);

        UserResponse user = userService.getUserByUsername(username);

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Retrieve current authenticated user information")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        log.debug("Get current user request received for: {}", principal.getUsername());

        UserResponse user = userService.getCurrentUser(principal);

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user information")
    @PreAuthorize("hasRole('ADMIN') or authentication.name == @userService.getUserById(#id).username")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Update user request received for ID: {}", id);

        UserResponse user = userService.updateUser(id, request);

        return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user", description = "Update current authenticated user information")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Update current user request received for: {}", principal.getUsername());

        UserResponse user = userService.updateCurrentUser(principal, request);

        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete user by ID (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        log.info("Delete user request received for ID: {}", id);

        userService.deleteUser(id);

        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate user", description = "Deactivate user account (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(@PathVariable Long id) {
        log.info("Deactivate user request received for ID: {}", id);

        UserResponse user = userService.deactivateUser(id);

        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", user));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate user", description = "Activate user account (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(@PathVariable Long id) {
        log.info("Activate user request received for ID: {}", id);

        UserResponse user = userService.activateUser(id);

        return ResponseEntity.ok(ApiResponse.success("User activated successfully", user));
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve paginated list of all users (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        log.debug("Get all users request - page: {}, size: {}", page, size);

        Page<UserResponse> users = userService.getAllUsers(page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping("/search")
    @Operation(summary = "Search users", description = "Search users with criteria (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> searchUsers(@Valid @RequestBody UserSearchRequest request) {
        log.debug("Search users request received with criteria: {}", request);

        Page<UserResponse> users = userService.searchUsers(request);

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping("/{id}/roles/{role}")
    @Operation(summary = "Add role to user", description = "Add a role to user (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> addRole(
            @PathVariable Long id,
            @PathVariable User.Role role) {
        log.info("Add role {} to user ID: {}", role, id);

        UserResponse user = userService.addRole(id, role);

        return ResponseEntity.ok(ApiResponse.success("Role added successfully", user));
    }

    @DeleteMapping("/{id}/roles/{role}")
    @Operation(summary = "Remove role from user", description = "Remove a role from user (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> removeRole(
            @PathVariable Long id,
            @PathVariable User.Role role) {
        log.info("Remove role {} from user ID: {}", role, id);

        UserResponse user = userService.removeRole(id, role);

        return ResponseEntity.ok(ApiResponse.success("Role removed successfully", user));
    }

    @PostMapping("/{id}/verify-email")
    @Operation(summary = "Verify user email", description = "Mark user email as verified (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@PathVariable Long id) {
        log.info("Verify email request for user ID: {}", id);

        userService.verifyEmail(id);

        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", null));
    }

    @PostMapping("/{id}/verify-phone")
    @Operation(summary = "Verify user phone", description = "Mark user phone as verified (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> verifyPhone(@PathVariable Long id) {
        log.info("Verify phone request for user ID: {}", id);

        userService.verifyPhone(id);

        return ResponseEntity.ok(ApiResponse.success("Phone verified successfully", null));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get user statistics", description = "Get user statistics (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats() {
        log.debug("Get user statistics request received");

        UserStatsResponse stats = userService.getUserStats();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}