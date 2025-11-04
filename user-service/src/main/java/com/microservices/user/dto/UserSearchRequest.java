package com.microservices.user.dto;

import com.microservices.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchRequest {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private User.Gender gender;
    private User.Role role;
    private Boolean active;
    private Boolean emailVerified;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdAfter;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdBefore;

    private String city;
    private String country;

    // Pagination
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDir = "desc";
}
