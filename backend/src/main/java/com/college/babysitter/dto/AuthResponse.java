package com.college.babysitter.dto;

import com.college.babysitter.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Login/signup result: the JWT (plus its type) and the account it belongs to.
 * The frontend stores the token and attaches it as a Bearer header.
 */
@Data
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String fullName;
    private String email;
    private Role role;

    /**
     * Creates an auth result with the default {@code Bearer} token type.
     *
     * @param token signed JWT
     * @param userId account id
     * @param fullName display name
     * @param email login email
     * @param role account role
     */
    public AuthResponse(String token, Long userId, String fullName, String email, Role role) {
        this.token = token;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }
}
