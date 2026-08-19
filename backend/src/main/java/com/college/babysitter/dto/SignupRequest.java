package com.college.babysitter.dto;

import com.college.babysitter.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Registration form. The role decides the account type; sitter signups also
 * get an empty profile row created for them.
 */
@Data
public class SignupRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String phone;

    // PARENT by default; set BABYSITTER if the person is a care provider
    @NotNull(message = "Role is required")
    private Role role;
}
