package com.m2i.BiblioRestApi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.m2i.BiblioRestApi.dto.LivreDTO;
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
@DisplayName("Tests d'intégration - LivreController")
class LivreControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private AuteurRepository auteurRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String jwtToken;
    private Auteur auteur;
    private Livre livre;

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

        // Créer un livre
        livre = Livre.builder()
                .titre("Les Misérables")
                .isbn("9782070409227")
                .anneePublication(1862)
                .nombreExemplaires(5)
                .auteur(auteur)
                .build();
        livre = livreRepository.save(livre);
    }

    @Test
    @DisplayName("GET /api/livres - Devrait retourner tous les livres")
    void getAllLivres_ShouldReturnAllBooks() throws Exception {
        mockMvc.perform(get("/api/livres")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titre").value("Les Misérables"))
                .andExpect(jsonPath("$[0].isbn").value("9782070409227"));
    }

    @Test
    @DisplayName("GET /api/livres/{id} - Devrait retourner un livre par ID")
    void getLivreById_ShouldReturnBook() throws Exception {
        mockMvc.perform(get("/api/livres/" + livre.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Les Misérables"))
                .andExpect(jsonPath("$.auteurId").value(auteur.getId()));
    }

    @Test
    @DisplayName("GET /api/livres/{id} - Devrait retourner 404 si livre n'existe pas")
    void getLivreById_WhenNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/livres/999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Livre non trouvé(e) avec id : '999'"));
    }

    @Test
    @DisplayName("GET /api/livres/isbn/{isbn} - Devrait retourner un livre par ISBN")
    void getLivreByIsbn_ShouldReturnBook() throws Exception {
        mockMvc.perform(get("/api/livres/isbn/9782070409227")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Les Misérables"));
    }

    @Test
    @DisplayName("POST /api/livres - Devrait créer un nouveau livre")
    void createLivre_WithValidData_ShouldCreateBook() throws Exception {
        LivreDTO newLivreDTO = LivreDTO.builder()
                .titre("Notre-Dame de Paris")
                .isbn("9782070413089")
                .anneePublication(1831)
                .nombreExemplaires(3)
                .auteurId(auteur.getId())
                .build();

        mockMvc.perform(post("/api/livres")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLivreDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.titre").value("Notre-Dame de Paris"))
                .andExpect(jsonPath("$.isbn").value("9782070413089"));
    }

    @Test
    @DisplayName("POST /api/livres - Devrait retourner 400 si données invalides")
    void createLivre_WithInvalidData_ShouldReturn400() throws Exception {
        LivreDTO invalidLivreDTO = LivreDTO.builder()
                .titre("") // Titre vide
                .isbn("invalid-isbn") // ISBN invalide
                .anneePublication(3000) // Année future invalide
                .auteurId(auteur.getId())
                .build();

        mockMvc.perform(post("/api/livres")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLivreDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    @DisplayName("POST /api/livres - Devrait retourner 409 si ISBN existe déjà")
    void createLivre_WithDuplicateIsbn_ShouldReturn409() throws Exception {
        LivreDTO duplicateLivreDTO = LivreDTO.builder()
                .titre("Autre livre")
                .isbn("9782070409227") // ISBN déjà existant
                .anneePublication(2000)
                .nombreExemplaires(1)
                .auteurId(auteur.getId())
                .build();

        mockMvc.perform(post("/api/livres")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateLivreDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Livre existe déjà avec isbn")));
    }

    @Test
    @DisplayName("PUT /api/livres/{id} - Devrait mettre à jour un livre")
    void updateLivre_WithValidData_ShouldUpdateBook() throws Exception {
        LivreDTO updateDTO = LivreDTO.builder()
                .titre("Les Misérables - Édition complète")
                .isbn("9782070409227")
                .anneePublication(1862)
                .nombreExemplaires(10)
                .auteurId(auteur.getId())
                .build();

        mockMvc.perform(put("/api/livres/" + livre.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Les Misérables - Édition complète"))
                .andExpect(jsonPath("$.nombreExemplaires").value(10));
    }

    @Test
    @DisplayName("PATCH /api/livres/{id}/exemplaires - Devrait mettre à jour le nombre d'exemplaires")
    void updateNombreExemplaires_ShouldUpdateQuantity() throws Exception {
        String updateJson = "{\"nombreExemplaires\": 15}";

        mockMvc.perform(patch("/api/livres/" + livre.getId() + "/exemplaires")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreExemplaires").value(15));
    }

    @Test
    @DisplayName("DELETE /api/livres/{id} - Devrait supprimer un livre")
    void deleteLivre_ShouldDeleteBook() throws Exception {
        mockMvc.perform(delete("/api/livres/" + livre.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        // Vérifier que le livre a été supprimé
        mockMvc.perform(get("/api/livres/" + livre.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/livres/search - Devrait rechercher par titre")
    void searchLivres_ByTitre_ShouldReturnMatchingBooks() throws Exception {
        mockMvc.perform(get("/api/livres/search")
                        .param("titre", "Misérables")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titre").value(containsString("Misérables")));
    }

    @Test
    @DisplayName("GET /api/livres/search - Devrait rechercher par année")
    void searchLivres_ByAnnee_ShouldReturnMatchingBooks() throws Exception {
        mockMvc.perform(get("/api/livres/search")
                        .param("anneeMin", "1850")
                        .param("anneeMax", "1900")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/livres - Sans authentification devrait retourner 403")
    void getAllLivres_WithoutAuth_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/livres"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/livres/auteur/{id} - Devrait retourner les livres d'un auteur")
    void getLivresByAuteur_ShouldReturnAuthorBooks() throws Exception {
        mockMvc.perform(get("/api/livres/auteur/" + auteur.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].auteurId").value(auteur.getId()));
    }
}
