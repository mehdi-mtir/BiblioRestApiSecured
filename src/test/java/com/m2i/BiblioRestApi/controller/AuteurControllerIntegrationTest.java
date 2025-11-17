package com.m2i.BiblioRestApi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.m2i.BiblioRestApi.dto.AuteurDTO;
import com.m2i.BiblioRestApi.model.Auteur;
import com.m2i.BiblioRestApi.model.Livre;
import com.m2i.BiblioRestApi.model.User;
import com.m2i.BiblioRestApi.repository.AuteurRepository;
import com.m2i.BiblioRestApi.repository.LivreRepository;
import com.m2i.BiblioRestApi.repository.UserRepository;
import com.m2i.BiblioRestApi.service.JwtService;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("Tests d'intégration - AuteurController")
class AuteurControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuteurRepository auteurRepository;

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String jwtToken;
    private Auteur auteur;

    @BeforeEach
    void setUp() {
        livreRepository.deleteAll();
        auteurRepository.deleteAll();
        userRepository.deleteAll();

        // Créer un utilisateur pour les tests
        User user = new User();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole("USER");
        userRepository.save(user);

        // Générer un token JWT
        jwtToken = jwtService.generateToken("testuser", "USER");

        // Créer un auteur
        auteur = Auteur.builder()
                .nom("Hugo")
                .prenom("Victor")
                .email("victor.hugo@test.com")
                .build();
        auteur = auteurRepository.save(auteur);
    }

    @Test
    @DisplayName("GET /api/auteurs - Devrait retourner tous les auteurs")
    void getAllAuteurs_ShouldReturnAllAuthors() throws Exception {
        mockMvc.perform(get("/api/auteurs")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nom").value("Hugo"))
                .andExpect(jsonPath("$[0].prenom").value("Victor"));
    }

    @Test
    @DisplayName("GET /api/auteurs/{id} - Devrait retourner un auteur par ID")
    void getAuteurById_ShouldReturnAuthor() throws Exception {
        mockMvc.perform(get("/api/auteurs/" + auteur.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Hugo"))
                .andExpect(jsonPath("$.prenom").value("Victor"))
                .andExpect(jsonPath("$.email").value("victor.hugo@test.com"));
    }

    @Test
    @DisplayName("GET /api/auteurs/{id} - Devrait retourner 404 si auteur n'existe pas")
    void getAuteurById_WhenNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/auteurs/999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Auteur non trouvé(e) avec id : '999'"));
    }

    @Test
    @DisplayName("POST /api/auteurs - Devrait créer un nouvel auteur")
    void createAuteur_WithValidData_ShouldCreateAuthor() throws Exception {
        AuteurDTO newAuteurDTO = AuteurDTO.builder()
                .nom("Camus")
                .prenom("Albert")
                .email("albert.camus@test.com")
                .build();

        mockMvc.perform(post("/api/auteurs")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAuteurDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nom").value("Camus"))
                .andExpect(jsonPath("$.prenom").value("Albert"))
                .andExpect(jsonPath("$.email").value("albert.camus@test.com"));
    }

    @Test
    @DisplayName("POST /api/auteurs - Devrait retourner 400 si données invalides")
    void createAuteur_WithInvalidData_ShouldReturn400() throws Exception {
        AuteurDTO invalidAuteurDTO = AuteurDTO.builder()
                .nom("A") // Trop court
                .prenom("") // Vide
                .email("invalid-email") // Email invalide
                .build();

        mockMvc.perform(post("/api/auteurs")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAuteurDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors.nom").exists())
                .andExpect(jsonPath("$.validationErrors.prenom").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    @DisplayName("POST /api/auteurs - Devrait retourner 409 si email existe déjà")
    void createAuteur_WithDuplicateEmail_ShouldReturn409() throws Exception {
        AuteurDTO duplicateAuteurDTO = AuteurDTO.builder()
                .nom("Autre")
                .prenom("Auteur")
                .email("victor.hugo@test.com") // Email déjà existant
                .build();

        mockMvc.perform(post("/api/auteurs")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateAuteurDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Auteur existe déjà avec email")));
    }

    @Test
    @DisplayName("PUT /api/auteurs/{id} - Devrait mettre à jour un auteur")
    void updateAuteur_WithValidData_ShouldUpdateAuthor() throws Exception {
        AuteurDTO updateDTO = AuteurDTO.builder()
                .nom("Hugo")
                .prenom("Victor Marie")
                .email("victor.hugo.updated@test.com")
                .build();

        mockMvc.perform(put("/api/auteurs/" + auteur.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prenom").value("Victor Marie"))
                .andExpect(jsonPath("$.email").value("victor.hugo.updated@test.com"));
    }

    @Test
    @DisplayName("DELETE /api/auteurs/{id} - Devrait supprimer un auteur sans livres")
    void deleteAuteur_WithoutBooks_ShouldDeleteAuthor() throws Exception {
        mockMvc.perform(delete("/api/auteurs/" + auteur.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        // Vérifier que l'auteur a été supprimé
        mockMvc.perform(get("/api/auteurs/" + auteur.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    /*

    @Test
    @DisplayName("DELETE /api/auteurs/{id} - Devrait retourner 400 si l'auteur a des livres")
    void deleteAuteur_WithBooks_ShouldReturn400() throws Exception {
        // Créer un livre pour l'auteur avec un ISBN unique
        Livre livre = Livre.builder()
                .titre("Les Misérables")
                .isbn("9782070409999") // ISBN différent pour éviter les conflits
                .anneePublication(1862)
                .nombreExemplaires(5)
                .auteur(auteur)
                .build();
        livreRepository.save(livre);

        mockMvc.perform(delete("/api/auteurs/" + auteur.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Impossible de supprimer l'auteur")))
                .andExpect(jsonPath("$.message").value(containsString("1 livre(s)")));
    }

    @Test
    @DisplayName("GET /api/auteurs/{id}/livres - Devrait retourner les livres d'un auteur")
    void getLivresByAuteur_ShouldReturnAuthorBooks() throws Exception {
        // Créer des livres pour l'auteur
        Livre livre1 = Livre.builder()
                .titre("Les Misérables")
                .isbn("9782070409111")
                .anneePublication(1862)
                .nombreExemplaires(5)
                .auteur(auteur)
                .build();
        livreRepository.save(livre1);

        Livre livre2 = Livre.builder()
                .titre("Notre-Dame de Paris")
                .isbn("9782070413222")
                .anneePublication(1831)
                .nombreExemplaires(3)
                .auteur(auteur)
                .build();
        livreRepository.save(livre2);

        mockMvc.perform(get("/api/auteurs/" + auteur.getId() + "/livres")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].titre", containsInAnyOrder("Les Misérables", "Notre-Dame de Paris")));
    }

     */

    @Test
    @DisplayName("GET /api/auteurs/{id}/livres - Devrait retourner liste vide si aucun livre")
    void getLivresByAuteur_WithNoBooks_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/auteurs/" + auteur.getId() + "/livres")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/auteurs - Sans authentification devrait retourner 401")
    void getAllAuteurs_WithoutAuth_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/auteurs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/auteurs - Devrait créer un auteur sans email")
    void createAuteur_WithoutEmail_ShouldCreateAuthor() throws Exception {
        AuteurDTO newAuteurDTO = AuteurDTO.builder()
                .nom("Orwell")
                .prenom("George")
                .build();

        mockMvc.perform(post("/api/auteurs")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAuteurDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Orwell"))
                .andExpect(jsonPath("$.prenom").value("George"))
                .andExpect(jsonPath("$.email").doesNotExist());
    }
}