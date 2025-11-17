package com.m2i.BiblioRestApi.security;

import com.m2i.BiblioRestApi.model.User;
import com.m2i.BiblioRestApi.repository.UserRepository;
import com.m2i.BiblioRestApi.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("Tests d'intégration - Sécurité")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Créer un utilisateur normal
        User user = new User();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole("USER");
        userRepository.save(user);
        userToken = jwtService.generateToken("user", "USER");

        // Créer un admin
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole("ADMIN");
        userRepository.save(admin);
        adminToken = jwtService.generateToken("admin", "ADMIN");
    }

    @Test
    @DisplayName("Endpoints /api/auth/** devraient être accessibles sans authentification")
    void authEndpoints_ShouldBePublic() throws Exception {
        // POST /api/auth/register avec des données manquantes devrait retourner 400 (bad request)
        // Ce qui prouve que l'endpoint est accessible sans authentification (pas de 401/403)
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // Body vide pour provoquer une validation error
                .andExpect(status().isBadRequest()); // 400 car données invalides, pas 401/403
    }

    @Test
    @DisplayName("GET /api/livres sans token devrait retourner 403")
    void getLivres_WithoutToken_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/livres"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/livres avec token valide devrait être autorisé")
    void getLivres_WithValidToken_ShouldBeAuthorized() throws Exception {
        mockMvc.perform(get("/api/livres")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

/*
    @Test
    @DisplayName("GET /api/livres avec token invalide devrait retourner 401")
    void getLivres_WithInvalidToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/livres")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

 */


    @Test
    @DisplayName("GET /api/livres avec Bearer manquant devrait retourner 403")
    void getLivres_WithoutBearerPrefix_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/livres")
                        .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/users devrait être accessible uniquement par ADMIN")
    void getUsers_AsUser_ShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/users en tant qu'ADMIN devrait être autorisé")
    void getUsers_AsAdmin_ShouldBeAuthorized() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/auteurs avec token USER devrait être autorisé")
    void getAuteurs_AsUser_ShouldBeAuthorized() throws Exception {
        mockMvc.perform(get("/api/auteurs")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/auteurs avec token ADMIN devrait être autorisé")
    void getAuteurs_AsAdmin_ShouldBeAuthorized() throws Exception {
        mockMvc.perform(get("/api/auteurs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    /*
    @Test
    @DisplayName("Token expiré devrait retourner 401")
    void request_WithExpiredToken_ShouldReturn401() throws Exception {
        // Créer un token expiré (on ne peut pas facilement le faire avec le service actuel,
        // donc on simule avec un token invalide)
        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNjAwMDAwMDAwLCJleHAiOjE2MDAwMDAwMDF9.invalid";

        mockMvc.perform(get("/api/livres")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

     */

    @Test
    @DisplayName("Header Authorization manquant devrait retourner 403")
    void request_WithoutAuthorizationHeader_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/livres"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Toutes les routes API (sauf /auth) devraient nécessiter authentification")
    void allApiRoutes_ExceptAuth_ShouldRequireAuthentication() throws Exception {
        // Tester plusieurs endpoints
        String[] endpoints = {
                "/api/livres",
                "/api/auteurs",
                "/api/livres/1",
                "/api/auteurs/1"
        };

        for (String endpoint : endpoints) {
            mockMvc.perform(get(endpoint))
                    .andExpect(status().isForbidden());
        }
    }

    /*
    @Test
    @DisplayName("Token avec mauvais format devrait retourner 401")
    void request_WithMalformedToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/livres")
                        .header("Authorization", "Bearer not.a.valid.jwt"))
                .andExpect(status().isUnauthorized());
    }

     */
}