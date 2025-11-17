package com.m2i.BiblioRestApi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.m2i.BiblioRestApi.dto.LoginRequest;
import com.m2i.BiblioRestApi.dto.RegisterRequest;
import com.m2i.BiblioRestApi.model.User;
import com.m2i.BiblioRestApi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("Tests d'intégration - AuthController")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/auth/register - Devrait enregistrer un nouvel utilisateur")
    void register_WithValidData_ShouldRegisterUser() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("newuser", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));

        // Vérifier que l'utilisateur a été créé en base
        User savedUser = userRepository.findByUsername("newuser").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("newuser");
        assertThat(savedUser.getRole()).isEqualTo("USER");
        assertThat(passwordEncoder.matches("password123", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("POST /api/auth/register - Devrait retourner erreur si username existe déjà")
    void register_WithExistingUsername_ShouldReturnError() throws Exception {
        // Créer un utilisateur existant
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        existingUser.setPassword(passwordEncoder.encode("password"));
        existingUser.setRole("USER");
        userRepository.save(existingUser);

        RegisterRequest registerRequest = new RegisterRequest("existinguser", "newpassword");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Username already exists")));
    }

    @Test
    @DisplayName("POST /api/auth/register-admin - Devrait enregistrer un admin")
    void registerAdmin_WithValidData_ShouldRegisterAdmin() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("admin", "adminpass");

        mockMvc.perform(post("/api/auth/register-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));

        // Vérifier que l'admin a été créé avec le bon rôle
        User savedUser = userRepository.findByUsername("admin").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getRole()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("POST /api/auth/login - Devrait authentifier avec credentials valides")
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        // Créer un utilisateur
        User user = new User();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole("USER");
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("testuser", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Devrait retourner 401 avec credentials invalides")
    void login_WithInvalidCredentials_ShouldReturn401() throws Exception {
        // Créer un utilisateur
        User user = new User();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole("USER");
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login - Devrait retourner 401 avec utilisateur inexistant")
    void login_WithNonExistentUser_ShouldReturn401() throws Exception {
        LoginRequest loginRequest = new LoginRequest("nonexistent", "password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login - Admin devrait avoir le rôle ADMIN dans le token")
    void login_AsAdmin_ShouldReturnAdminRole() throws Exception {
        // Créer un admin
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("adminpass"));
        admin.setRole("ADMIN");
        userRepository.save(admin);

        LoginRequest loginRequest = new LoginRequest("admin", "adminpass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("Workflow complet - Enregistrement puis connexion")
    void completeWorkflow_RegisterThenLogin_ShouldWork() throws Exception {
        // 1. Enregistrement
        RegisterRequest registerRequest = new RegisterRequest("workflow", "pass123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // 2. Connexion
        LoginRequest loginRequest = new LoginRequest("workflow", "pass123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("workflow"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Devrait encoder le mot de passe")
    void register_ShouldEncodePassword() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("testenc", "plainpassword");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        User savedUser = userRepository.findByUsername("testenc").orElse(null);
        assertThat(savedUser).isNotNull();
        // Le mot de passe ne doit pas être stocké en clair
        assertThat(savedUser.getPassword()).isNotEqualTo("plainpassword");
        // Mais doit matcher avec l'encodeur
        assertThat(passwordEncoder.matches("plainpassword", savedUser.getPassword())).isTrue();
    }
}