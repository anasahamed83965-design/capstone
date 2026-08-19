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
import com.college.babysitter.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BabysitterRepository babysitterRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private SignupRequest parentRequest;

    @BeforeEach
    void setUp() {
        parentRequest = new SignupRequest();
        parentRequest.setFullName("Maria Lopez");
        parentRequest.setEmail("maria@example.com");
        parentRequest.setPassword("secret12");
        parentRequest.setPhone("555-0100");
        parentRequest.setRole(Role.PARENT);
    }

    @Test
    void signup_createsParentAccount() {
        when(userRepository.existsByEmail("maria@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret12")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });
        when(jwtService.generateToken(any(), eq(10L), eq("PARENT"))).thenReturn("jwt-token");

        AuthResponse response = authService.signup(parentRequest);

        assertNotNull(response.getToken());
        assertEquals("jwt-token", response.getToken());
        assertEquals(10L, response.getUserId());
        assertEquals(Role.PARENT, response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
        verify(babysitterRepository, never()).save(any());
    }

    @Test
    void signup_duplicateEmail_throws() {
        when(userRepository.existsByEmail("maria@example.com")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> authService.signup(parentRequest));

        assertTrue(ex.getMessage().toLowerCase().contains("already exists"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_babysitter_createsEmptyProfile() {
        parentRequest.setRole(Role.BABYSITTER);
        when(userRepository.existsByEmail("maria@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret12")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });
        when(jwtService.generateToken(any(), eq(10L), eq("BABYSITTER"))).thenReturn("jwt-token");

        authService.signup(parentRequest);

        verify(babysitterRepository, times(1)).save(any(Babysitter.class));
    }

    @Test
    void login_returnsTokenForExistingUser() {
        LoginRequest login = new LoginRequest();
        login.setEmail("maria@example.com");
        login.setPassword("secret12");

        User user = new User("Maria Lopez", "maria@example.com", "hashed", "555-0100", Role.PARENT);
        user.setId(10L);
        when(userRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(), eq(10L), eq("PARENT"))).thenReturn("jwt-token");

        AuthResponse response = authService.login(login);

        assertEquals("maria@example.com", response.getEmail());
        assertEquals(10L, response.getUserId());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void seedAdmin_createsWhenMissing() {
        when(userRepository.existsByEmail("admin@babysitter.app")).thenReturn(false);
        when(passwordEncoder.encode("admin123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(99L);
            return u;
        });
        when(jwtService.generateToken(any(), eq(99L), eq("ADMIN"))).thenReturn("jwt-token");

        AuthResponse response = authService.seedAdmin();

        assertEquals(Role.ADMIN, response.getRole());
        assertEquals("admin@babysitter.app", response.getEmail());
    }
}
