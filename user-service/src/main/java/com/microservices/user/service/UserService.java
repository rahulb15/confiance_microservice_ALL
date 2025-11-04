package com.microservices.user.service;

import com.microservices.common.core.dto.UserPrincipal;
import com.microservices.common.core.enums.ResponseCode;
import com.microservices.common.core.exception.BusinessException;
import com.microservices.common.core.exception.ResourceNotFoundException;
import com.microservices.user.dto.*;
import com.microservices.user.entity.User;
import com.microservices.user.mapper.UserMapper;
import com.microservices.user.repository.UserRepository;
import com.microservices.user.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthServiceClient authServiceClient;

    @Transactional
    public UserResponse createUser(UserRequest request) {
        log.info("Creating user with username: {}", request.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ResponseCode.USERNAME_ALREADY_EXISTS, "Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ResponseCode.EMAIL_ALREADY_EXISTS, "Email already exists");
        }

        User user = userMapper.toEntity(request);
        user.addRole(User.Role.USER); // Default role

        User savedUser = userRepository.save(user);

        log.info("User created successfully with ID: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UserPrincipal principal) {
        return getUserByUsername(principal.getUsername());
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        // Check if email is being updated and already exists
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException(ResponseCode.EMAIL_ALREADY_EXISTS, "Email already exists");
            }
            user.setEmailVerified(false); // Reset email verification
        }

        // Check if phone is being updated
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            user.setPhoneVerified(false); // Reset phone verification
        }

        userMapper.updateEntityFromRequest(request, user);

        User updatedUser = userRepository.save(user);

        log.info("User updated successfully: {}", updatedUser.getId());
        return userMapper.toResponse(updatedUser);
    }

    @Transactional
    public UserResponse updateCurrentUser(UserPrincipal principal, UserUpdateRequest request) {
        User user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", principal.getUsername()));

        return updateUser(user.getId(), request);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        userRepository.delete(user);

        log.info("User deleted successfully: {}", id);
    }

    @Transactional
    public UserResponse deactivateUser(Long id) {
        log.info("Deactivating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        user.setActive(false);
        User updatedUser = userRepository.save(user);

        log.info("User deactivated successfully: {}", id);
        return userMapper.toResponse(updatedUser);
    }

    @Transactional
    public UserResponse activateUser(Long id) {
        log.info("Activating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        user.setActive(true);
        User updatedUser = userRepository.save(user);

        log.info("User activated successfully: {}", id);
        return userMapper.toResponse(updatedUser);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(int page, int size, String sortBy, String sortDir) {
        log.debug("Fetching all users - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<User> users = userRepository.findAll(pageable);

        return users.map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(UserSearchRequest searchRequest) {
        log.debug("Searching users with criteria: {}", searchRequest);

        Sort.Direction direction = searchRequest.getSortDir().equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(
                searchRequest.getPage(),
                searchRequest.getSize(),
                Sort.by(direction, searchRequest.getSortBy())
        );

        Specification<User> spec = UserSpecification.buildSpecification(searchRequest);
        Page<User> users = userRepository.findAll(spec, pageable);

        return users.map(userMapper::toResponse);
    }

    @Transactional
    public UserResponse addRole(Long userId, User.Role role) {
        log.info("Adding role {} to user ID: {}", role, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        user.addRole(role);
        User updatedUser = userRepository.save(user);

        log.info("Role {} added to user: {}", role, userId);
        return userMapper.toResponse(updatedUser);
    }

    @Transactional
    public UserResponse removeRole(Long userId, User.Role role) {
        log.info("Removing role {} from user ID: {}", role, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        if (role == User.Role.USER && user.getRoles().size() == 1) {
            throw new BusinessException(ResponseCode.OPERATION_NOT_ALLOWED, "Cannot remove the last USER role");
        }

        user.removeRole(role);
        User updatedUser = userRepository.save(user);

        log.info("Role {} removed from user: {}", role, userId);
        return userMapper.toResponse(updatedUser);
    }

    @Transactional
    public void verifyEmail(Long userId) {
        log.info("Verifying email for user ID: {}", userId);

        userRepository.markEmailAsVerified(userId);

        log.info("Email verified for user: {}", userId);
    }

    @Transactional
    public void verifyPhone(Long userId) {
        log.info("Verifying phone for user ID: {}", userId);

        userRepository.markPhoneAsVerified(userId);

        log.info("Phone verified for user: {}", userId);
    }

    @Transactional
    public void updateLastLogin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        userRepository.updateLastLoginTime(user.getId(), LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countActiveUsers();
        long newUsersThisMonth = userRepository.countUsersCreatedSince(LocalDateTime.now().minusMonths(1));

        return UserStatsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(totalUsers - activeUsers)
                .newUsersThisMonth(newUsersThisMonth)
                .build();
    }
}
