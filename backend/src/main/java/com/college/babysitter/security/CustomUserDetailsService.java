package com.college.babysitter.security;

import com.college.babysitter.model.User;
import com.college.babysitter.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Bridges Spring Security login to the application {@code users} table:
 * accounts are looked up by email and wrapped as {@link AppUserPrincipal}.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Wires the user lookup.
     *
     * @param userRepository user persistence
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads an account by email for authentication.
     *
     * @param email account email (the login identity)
     * @return the security principal
     * @throws UsernameNotFoundException when no account uses the email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return new AppUserPrincipal(user);
    }
}
