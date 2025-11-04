package com.microservices.user.specification;

import com.microservices.user.dto.UserSearchRequest;
import com.microservices.user.entity.User;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> buildSpecification(UserSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("username")),
                        "%" + request.getUsername().toLowerCase() + "%"
                ));
            }

            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%" + request.getEmail().toLowerCase() + "%"
                ));
            }

            if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")),
                        "%" + request.getFirstName().toLowerCase() + "%"
                ));
            }

            if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")),
                        "%" + request.getLastName().toLowerCase() + "%"
                ));
            }

            if (request.getGender() != null) {
                predicates.add(criteriaBuilder.equal(root.get("gender"), request.getGender()));
            }

            if (request.getRole() != null) {
                predicates.add(criteriaBuilder.isMember(request.getRole(), root.get("roles")));
            }

            if (request.getActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), request.getActive()));
            }

            if (request.getEmailVerified() != null) {
                predicates.add(criteriaBuilder.equal(root.get("emailVerified"), request.getEmailVerified()));
            }

            if (request.getCreatedAfter() != null) {
                LocalDateTime startDateTime = request.getCreatedAfter().atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
            }

            if (request.getCreatedBefore() != null) {
                LocalDateTime endDateTime = request.getCreatedBefore().atTime(23, 59, 59);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
            }

            if (request.getCity() != null && !request.getCity().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("address").get("city")),
                        "%" + request.getCity().toLowerCase() + "%"
                ));
            }

            if (request.getCountry() != null && !request.getCountry().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("address").get("country")),
                        "%" + request.getCountry().toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

