package com.college.babysitter.dto;

import com.college.babysitter.model.Role;
import com.college.babysitter.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Account view for the admin user lists. Carries identity and role info
 * only - never the password hash.
 */
@Data
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;

    /**
     * Converts an account entity to the admin view.
     *
     * @param user account entity
     * @return the admin view
     */
    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt());
    }
}
