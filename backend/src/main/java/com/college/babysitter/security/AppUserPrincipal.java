package com.college.babysitter.security;

import com.college.babysitter.model.Role;
import com.college.babysitter.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security view of an application {@link User}. Snapshots the id,
 * email, password hash, role and active flag so the security context never
 * holds a JPA entity (which would leak lazy-loading into filters).
 */
public class AppUserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final boolean active;

    /**
     * Snapshots the security-relevant fields of an account.
     *
     * @param user the account to wrap
     */
    public AppUserPrincipal(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.role = user.getRole();
        this.active = user.isActive();
    }

    /**
     * Returns the account id (used by controllers for ownership checks).
     *
     * @return account id
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the account role (used for role-based booking rules).
     *
     * @return account role
     */
    public Role getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
