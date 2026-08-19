package com.college.babysitter.service;

import com.college.babysitter.dto.AuthResponse;
import com.college.babysitter.dto.LoginRequest;
import com.college.babysitter.dto.SignupRequest;
import com.college.babysitter.exception.ApiException;
import com.college.babysitter.model.Babysitter;
import com.college.babysitter.model.Role;
import com.college.babysitter.model.User;
import com.college.babysitter.repository.BabysitterRepository;
import com.college.babysitter.repository.UserRepository;
import com.college.babysitter.security.AppUserPrincipal;
import com.college.babysitter.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Account business logic: registration, login and the one-click admin seed.
 * Passwords are always bcrypt-hashed here; raw passwords never reach the database.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final BabysitterRepository babysitterRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Wires the auth collaborators (constructor injection keeps the class unit-testable).
     *
     * @param userRepository user persistence
     * @param babysitterRepository sitter-profile persistence
     * @param passwordEncoder bcrypt password hashing
     * @param jwtService token creation and validation
     * @param authenticationManager Spring Security credential checking
     */
    public AuthService(UserRepository userRepository,
                       BabysitterRepository babysitterRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.babysitterRepository = babysitterRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Registers a new account. Babysitter signups additionally get an empty,
     * unverified profile row so the sitter dashboard works immediately.
     *
     * @param request validated signup payload
     * @return the new account plus its JWT
     * @throws ApiException with 400 if the email is already taken
     */
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApiException.badRequest("An account with this email already exists");
        }

        User user = new User(
                request.getFullName().trim(),
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhone(),
                request.getRole());

        userRepository.save(user);

        if (user.getRole() == Role.BABYSITTER) {
            // profile starts unverified; admin approves before it goes public
            babysitterRepository.save(new Babysitter(user, "", 0, BigDecimal.ZERO));
        }

        log.info("User signed up: {} ({})", user.getEmail(), user.getRole());
        return buildAuthResponse(user);
    }

    /**
     * Verifies credentials through Spring Security and issues a fresh JWT.
     * Wrong credentials surface as 401 via the global exception handler.
     *
     * @param request validated login payload
     * @return the account plus its JWT
     */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().trim().toLowerCase(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    /**
     * Creates the default platform admin if absent, otherwise returns it.
     * Idempotent by design so setup can be re-run safely.
     *
     * @return the admin account plus its JWT
     */
    @Transactional
    public AuthResponse seedAdmin() {
        if (userRepository.existsByEmail("admin@babysitter.app")) {
            return buildAuthResponse(userRepository.findByEmail("admin@babysitter.app").get());
        }
        User admin = new User("Platform Admin", "admin@babysitter.app",
                passwordEncoder.encode("admin123"), "0000000000", Role.ADMIN);
        userRepository.save(admin);
        log.info("Admin account seeded");
        return buildAuthResponse(admin);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(new AppUserPrincipal(user), user.getId(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
