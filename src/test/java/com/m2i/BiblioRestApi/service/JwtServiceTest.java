package com.m2i.BiblioRestApi.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Tests unitaires - JwtService")
class JwtServiceTest {

    private JwtService jwtService;
    private String validToken;
    private final String username = "testuser";
    private final String role = "USER";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        validToken = jwtService.generateToken(username, role);
    }

    @Test
    @DisplayName("Devrait générer un token JWT valide")
    void generateToken_ShouldCreateValidToken() {
        // When
        String token = jwtService.generateToken(username, role);

        // Then
        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3); // JWT a 3 parties séparées par des points
    }

    @Test
    @DisplayName("Devrait extraire le username du token")
    void extractUsername_ShouldReturnCorrectUsername() {
        // When
        String extractedUsername = jwtService.extractUsername(validToken);

        // Then
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("Devrait extraire le rôle du token")
    void extractRole_ShouldReturnCorrectRole() {
        // When
        String extractedRole = jwtService.extractRole(validToken);

        // Then
        assertThat(extractedRole).isEqualTo(role);
    }

    @Test
    @DisplayName("Devrait extraire la date d'expiration du token")
    void extractExpiration_ShouldReturnFutureDate() {
        // When
        Date expiration = jwtService.extractExpiration(validToken);

        // Then
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    @DisplayName("Devrait valider un token valide")
    void isTokenValid_WithValidToken_ShouldReturnTrue() {
        // When
        boolean isValid = jwtService.isTokenValid(validToken, username);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Devrait invalider un token avec un username différent")
    void isTokenValid_WithDifferentUsername_ShouldReturnFalse() {
        // When
        boolean isValid = jwtService.isTokenValid(validToken, "wronguser");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Devrait valider la structure du token")
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // When
        boolean isValid = jwtService.validateToken(validToken);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Devrait invalider un token malformé")
    void validateToken_WithMalformedToken_ShouldReturnFalse() {
        // Given
        String malformedToken = "invalid.token.here";

        // When
        boolean isValid = jwtService.validateToken(malformedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Devrait détecter un token expiré")
    void isTokenValid_WithExpiredToken_ShouldReturnFalse() throws InterruptedException {
        // Given - Créer un token avec une durée d'expiration très courte
        SecretKey key = Keys.hmacShaKeyFor(
                "maCleSecreteSuperSecuriseePourMonApplicationSpringBootM2i2025".getBytes()
        );

        String expiredToken = Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis() - 2000))
                .expiration(new Date(System.currentTimeMillis() - 1000)) // Expiré il y a 1 seconde
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        // When & Then
        assertThatThrownBy(() -> jwtService.extractUsername(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("Devrait générer des tokens différents pour des utilisateurs différents")
    void generateToken_ForDifferentUsers_ShouldGenerateDifferentTokens() {
        // When
        String token1 = jwtService.generateToken("user1", "USER");
        String token2 = jwtService.generateToken("user2", "ADMIN");

        // Then
        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtService.extractUsername(token1)).isEqualTo("user1");
        assertThat(jwtService.extractUsername(token2)).isEqualTo("user2");
        assertThat(jwtService.extractRole(token1)).isEqualTo("USER");
        assertThat(jwtService.extractRole(token2)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Le token devrait contenir la date d'émission")
    void generateToken_ShouldContainIssuedAt() {
        // Given
        Date before = new Date();

        // When
        String token = jwtService.generateToken(username, role);
        Date expiration = jwtService.extractExpiration(token);

        // Then
        Date after = new Date();
        assertThat(expiration).isAfter(before);
        assertThat(expiration).isAfter(after);
    }
}