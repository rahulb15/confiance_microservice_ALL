package com.microservices.auth.repository;

import com.microservices.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.username = :username")
    void updateLastLoginTime(@Param("username") String username, @Param("loginTime") LocalDateTime loginTime);

    @Modifying
    @Query("UPDATE User u SET u.loginAttempts = :attempts WHERE u.username = :username")
    void updateLoginAttempts(@Param("username") String username, @Param("attempts") Integer attempts);

    @Modifying
    @Query("UPDATE User u SET u.accountNonLocked = :locked, u.lockedAt = :lockedAt WHERE u.username = :username")
    void updateAccountLocked(@Param("username") String username, @Param("locked") boolean locked, @Param("lockedAt") LocalDateTime lockedAt);
}
