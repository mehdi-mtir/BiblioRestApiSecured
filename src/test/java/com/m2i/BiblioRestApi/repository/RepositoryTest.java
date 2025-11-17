package com.m2i.BiblioRestApi.repository;

import com.m2i.BiblioRestApi.model.Auteur;
import com.m2i.BiblioRestApi.model.Livre;
import com.m2i.BiblioRestApi.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests des Repositories")
class RepositoryTest {

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private AuteurRepository auteurRepository;

    @Autowired
    private UserRepository userRepository;

    private Auteur auteur;

    @BeforeEach
    void setUp() {
        livreRepository.deleteAll();
        auteurRepository.deleteAll();
        userRepository.deleteAll();

        auteur = Auteur.builder()
                .nom("Hugo")
                .prenom("Victor")
                .email("victor.hugo@test.com")
                .build();
        auteur = auteurRepository.save(auteur);
    }

    // Tests LivreRepository
    @Test
    @DisplayName("LivreRepository - findByIsbn devrait trouver un livre")
    void livreRepository_findByIsbn_ShouldFindBook() {
        // Given
        Livre livre = Livre.builder()
                .titre("Les Misérables")
                .isbn("9782070409228")
                .anneePublication(1862)
                .nombreExemplaires(5)
                .auteur(auteur)
                .build();
        livreRepository.save(livre);

        // When
        Optional<Livre> found = livreRepository.findByIsbn("9782070409228");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTitre()).isEqualTo("Les Misérables");
    }

    @Test
    @DisplayName("LivreRepository - existsByIsbn devrait retourner true si livre existe")
    void livreRepository_existsByIsbn_ShouldReturnTrue() {
        // Given
        Livre livre = Livre.builder()
                .titre("Les Misérables")
                .isbn("9782070409228")
                .anneePublication(1862)
                .nombreExemplaires(5)
                .auteur(auteur)
                .build();
        livreRepository.save(livre);

        // When
        boolean exists = livreRepository.existsByIsbn("9782070409228");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("LivreRepository - findByAuteurId devrait retourner les livres d'un auteur")
    void livreRepository_findByAuteurId_ShouldReturnAuthorBooks() {
        // Given
        Livre livre1 = Livre.builder()
                .titre("Les Misérables")
                .isbn("9782070409228")
                .anneePublication(1862)
                .nombreExemplaires(5)
                .auteur(auteur)
                .build();
        livreRepository.save(livre1);

        Livre livre2 = Livre.builder()
                .titre("Notre-Dame de Paris")
                .isbn("9782070413089")
                .anneePublication(1831)
                .nombreExemplaires(3)
                .auteur(auteur)
                .build();
        livreRepository.save(livre2);

        // When
        List<Livre> livres = livreRepository.findByAuteurId(auteur.getId());

        // Then
        assertThat(livres).hasSize(2);
        assertThat(livres).extracting(Livre::getTitre)
                .containsExactlyInAnyOrder("Les Misérables", "Notre-Dame de Paris");
    }

    @Test
    @DisplayName("LivreRepository - findByTitreContainingIgnoreCase devrait rechercher par titre")
    void livreRepository_findByTitreContaining_ShouldFindMatchingBooks() {
        // Given
        Livre livre = Livre.builder()
                .titre("Les Misérables")
                .isbn("9782070409228")
                .anneePublication(1862)
                .nombreExemplaires(5)
                .auteur(auteur)
                .build();
        livreRepository.save(livre);

        // When
        List<Livre> found = livreRepository.findByTitreContainingIgnoreCase("misérables");

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getTitre()).isEqualTo("Les Misérables");
    }

    @Test
    @DisplayName("LivreRepository - findByAnneePublicationBetween devrait filtrer par période")
    void livreRepository_findByAnneePublicationBetween_ShouldFilterByYear() {
        // Given
        Livre livre1 = Livre.builder()
                .titre("Les Misérables")
                .isbn("9782070409228")
                .anneePublication(1862)
                .nombreExemplaires(5)
                .auteur(auteur)
                .build();
        livreRepository.save(livre1);

        Livre livre2 = Livre.builder()
                .titre("Notre-Dame de Paris")
                .isbn("9782070413089")
                .anneePublication(1831)
                .nombreExemplaires(3)
                .auteur(auteur)
                .build();
        livreRepository.save(livre2);

        // When
        List<Livre> found = livreRepository.findByAnneePublicationBetween(1850, 1900);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getTitre()).isEqualTo("Les Misérables");
    }

    // Tests AuteurRepository
    @Test
    @DisplayName("AuteurRepository - findByEmail devrait trouver un auteur")
    void auteurRepository_findByEmail_ShouldFindAuthor() {
        // When
        Optional<Auteur> found = auteurRepository.findByEmail("victor.hugo@test.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getNom()).isEqualTo("Hugo");
    }

    @Test
    @DisplayName("AuteurRepository - existsByEmail devrait retourner true si email existe")
    void auteurRepository_existsByEmail_ShouldReturnTrue() {
        // When
        boolean exists = auteurRepository.existsByEmail("victor.hugo@test.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("AuteurRepository - existsByEmail devrait retourner false si email n'existe pas")
    void auteurRepository_existsByEmail_ShouldReturnFalse() {
        // When
        boolean exists = auteurRepository.existsByEmail("nonexistent@test.com");

        // Then
        assertThat(exists).isFalse();
    }

    // Tests UserRepository
    @Test
    @DisplayName("UserRepository - findByUsername devrait trouver un utilisateur")
    void userRepository_findByUsername_ShouldFindUser() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRole("USER");
        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByUsername("testuser");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getRole()).isEqualTo("USER");
    }

    @Test
    @DisplayName("UserRepository - existsByUsername devrait retourner true si username existe")
    void userRepository_existsByUsername_ShouldReturnTrue() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRole("USER");
        userRepository.save(user);

        // When
        boolean exists = userRepository.existsByUsername("testuser");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("UserRepository - existsByUsername devrait retourner false si username n'existe pas")
    void userRepository_existsByUsername_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Relations - Livre devrait charger son auteur")
    void relations_livre_ShouldLoadAuteur() {
        // Given
        Livre livre = Livre.builder()
                .titre("Les Misérables")
                .isbn("9782070409228")
                .anneePublication(1862)
                .nombreExemplaires(5)
                .auteur(auteur)
                .build();
        livre = livreRepository.save(livre);

        // When
        Livre found = livreRepository.findById(livre.getId()).orElse(null);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getAuteur()).isNotNull();
        assertThat(found.getAuteur().getNom()).isEqualTo("Hugo");
    }

    /*
    @Test
    @DisplayName("Cascade - Suppression d'un auteur devrait supprimer ses livres")
    void cascade_deleteAuteur_ShouldDeleteBooks() {
        // Given
        Livre livre = Livre.builder()
                .titre("Les Misérables")
                .isbn("9782070409228")
                .anneePublication(1862)
                .nombreExemplaires(5)
                .auteur(auteur)
                .build();
        livreRepository.save(livre);

        // When
        auteurRepository.delete(auteur);

        // Then
        List<Livre> livres = livreRepository.findAll();
        assertThat(livres).isEmpty();
    }

     */
}
