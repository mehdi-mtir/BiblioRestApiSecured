package com.m2i.BiblioRestApi.service;

import com.m2i.BiblioRestApi.dto.LoginRequest;
import com.m2i.BiblioRestApi.dto.LoginResponse;
import com.m2i.BiblioRestApi.model.User;
import com.m2i.BiblioRestApi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - AuthenticationService")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User user;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole("USER");

        loginRequest = new LoginRequest("testuser", "password123");
    }

    @Test
    @DisplayName("Devrait authentifier un utilisateur avec des credentials valides")
    void authenticate_WithValidCredentials_ShouldReturnLoginResponse() {
        // Given
        String token = "jwt.token.here";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("testuser", "USER")).thenReturn(token);

        // When
        LoginResponse response = authenticationService.authenticate(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getRole()).isEqualTo("USER");
        assertThat(response.getType()).isEqualTo("Bearer");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(jwtService, times(1)).generateToken("testuser", "USER");
    }

    @Test
    @DisplayName("Devrait lever exception avec des credentials invalides")
    void authenticate_WithInvalidCredentials_ShouldThrowException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Bad credentials");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUsername(any());
        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    @DisplayName("Devrait enregistrer un nouvel utilisateur")
    void registerUser_WithNewUsername_ShouldRegisterSuccessfully() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String result = authenticationService.registerUser("newuser", "password123", "USER");

        // Then
        assertThat(result).isEqualTo("User registered successfully");

        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Devrait lever exception si le username existe déjà")
    void registerUser_WithExistingUsername_ShouldThrowException() {
        // Given
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authenticationService.registerUser("existinguser", "password123", "USER"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, times(1)).existsByUsername("existinguser");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Devrait enregistrer un utilisateur avec le rôle ADMIN")
    void registerUser_WithAdminRole_ShouldRegisterAdmin() {
        // Given
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(passwordEncoder.encode("adminpass")).thenReturn("encodedAdminPass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertThat(savedUser.getRole()).isEqualTo("ADMIN");
            return savedUser;
        });

        // When
        String result = authenticationService.registerUser("admin", "adminpass", "ADMIN");

        // Then
        assertThat(result).isEqualTo("User registered successfully");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Devrait lever exception si l'utilisateur n'est pas trouvé après authentification")
    void authenticate_WhenUserNotFoundAfterAuth_ShouldThrowException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(jwtService, never()).generateToken(any(), any());
    }
}