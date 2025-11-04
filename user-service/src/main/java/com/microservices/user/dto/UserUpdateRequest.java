package com.microservices.user.dto;

import com.microservices.user.entity.Address;
import com.microservices.user.entity.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Email(message = "Email should be valid")
    private String email;

    @Size(max = 50, message = "First name must be less than 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String lastName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number should be valid")
    private String phoneNumber;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private User.Gender gender;

    @Valid
    private Address address;

    @Size(max = 500, message = "Bio must be less than 500 characters")
    private String bio;

    @AssertTrue(message = "User must be at least 13 years old")
    private boolean isValidAge() {
        if (dateOfBirth == null) return true;
        return dateOfBirth.isBefore(LocalDate.now().minusYears(13));
    }
}
