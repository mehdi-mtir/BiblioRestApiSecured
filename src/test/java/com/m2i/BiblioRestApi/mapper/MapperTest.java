package com.m2i.BiblioRestApi.mapper;

import com.m2i.BiblioRestApi.dto.AuteurDTO;
import com.m2i.BiblioRestApi.dto.LivreDTO;
import com.m2i.BiblioRestApi.model.Auteur;
import com.m2i.BiblioRestApi.model.Livre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests unitaires - Mappers")
class MapperTest {

    private LivreMapper livreMapper;
    private AuteurMapper auteurMapper;
    private Auteur auteur;
    private Livre livre;

    @BeforeEach
    void setUp() {
        livreMapper = new LivreMapper();
        auteurMapper = new AuteurMapper();

        auteur = Auteur.builder()
                .id(1L)
                .nom("Hugo")
                .prenom("Victor")
                .email("victor.hugo@test.com")
                .livres(new ArrayList<>())
                .build();

        livre = Livre.builder()
                .id(1L)
                .titre("Les Misérables")
                .isbn("9782070409228")
                .anneePublication(1862)
                .nombreExemplaires(5)
                .auteur(auteur)
                .build();
    }

    // Tests LivreMapper
    @Test
    @DisplayName("LivreMapper - toDTO devrait convertir Livre en LivreDTO")
    void livreMapper_toDTO_ShouldConvertToDTO() {
        // When
        LivreDTO dto = livreMapper.toDTO(livre);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitre()).isEqualTo("Les Misérables");
        assertThat(dto.getIsbn()).isEqualTo("9782070409228");
        assertThat(dto.getAnneePublication()).isEqualTo(1862);
        assertThat(dto.getNombreExemplaires()).isEqualTo(5);
        assertThat(dto.getAuteurId()).isEqualTo(1L);
        assertThat(dto.getNomCompletAuteur()).isEqualTo("Victor Hugo");
    }

    @Test
    @DisplayName("LivreMapper - toDTO avec livre null devrait retourner null")
    void livreMapper_toDTO_WithNull_ShouldReturnNull() {
        // When
        LivreDTO dto = livreMapper.toDTO(null);

        // Then
        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("LivreMapper - toEntity devrait convertir LivreDTO en Livre")
    void livreMapper_toEntity_ShouldConvertToEntity() {
        // Given
        LivreDTO dto = LivreDTO.builder()
                .id(1L)
                .titre("Les Misérables")
                .isbn("9782070409228")
                .anneePublication(1862)
                .nombreExemplaires(5)
                .auteurId(1L)
                .build();

        // When
        Livre entity = livreMapper.toEntity(dto, auteur);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getTitre()).isEqualTo("Les Misérables");
        assertThat(entity.getIsbn()).isEqualTo("9782070409228");
        assertThat(entity.getAnneePublication()).isEqualTo(1862);
        assertThat(entity.getNombreExemplaires()).isEqualTo(5);
        assertThat(entity.getAuteur()).isEqualTo(auteur);
    }

    @Test
    @DisplayName("LivreMapper - updateEntityFromDTO devrait mettre à jour l'entité")
    void livreMapper_updateEntityFromDTO_ShouldUpdateEntity() {
        // Given
        LivreDTO dto = LivreDTO.builder()
                .titre("Les Misérables - Edition Complète")
                .isbn("9782070409229")
                .anneePublication(1863)
                .nombreExemplaires(10)
                .build();

        Auteur newAuteur = Auteur.builder().id(2L).nom("Autre").prenom("Auteur").build();

        // When
        livreMapper.updateEntityFromDTO(dto, livre, newAuteur);

        // Then
        assertThat(livre.getTitre()).isEqualTo("Les Misérables - Edition Complète");
        assertThat(livre.getIsbn()).isEqualTo("9782070409229");
        assertThat(livre.getAnneePublication()).isEqualTo(1863);
        assertThat(livre.getNombreExemplaires()).isEqualTo(10);
        assertThat(livre.getAuteur()).isEqualTo(newAuteur);
    }

    @Test
    @DisplayName("LivreMapper - toDTOList devrait convertir une liste")
    void livreMapper_toDTOList_ShouldConvertList() {
        // Given
        Livre livre2 = Livre.builder()
                .id(2L)
                .titre("Notre-Dame de Paris")
                .isbn("9782070413089")
                .anneePublication(1831)
                .nombreExemplaires(3)
                .auteur(auteur)
                .build();
        List<Livre> livres = Arrays.asList(livre, livre2);

        // When
        List<LivreDTO> dtos = livreMapper.toDTOList(livres);

        // Then
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getTitre()).isEqualTo("Les Misérables");
        assertThat(dtos.get(1).getTitre()).isEqualTo("Notre-Dame de Paris");
    }

    // Tests AuteurMapper
    @Test
    @DisplayName("AuteurMapper - toDTO devrait convertir Auteur en AuteurDTO")
    void auteurMapper_toDTO_ShouldConvertToDTO() {
        // Given
        auteur.getLivres().add(livre);

        // When
        AuteurDTO dto = auteurMapper.toDTO(auteur);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getNom()).isEqualTo("Hugo");
        assertThat(dto.getPrenom()).isEqualTo("Victor");
        assertThat(dto.getEmail()).isEqualTo("victor.hugo@test.com");
        assertThat(dto.getNombreLivres()).isEqualTo(1);
    }

    @Test
    @DisplayName("AuteurMapper - toDTO avec auteur null devrait retourner null")
    void auteurMapper_toDTO_WithNull_ShouldReturnNull() {
        // When
        AuteurDTO dto = auteurMapper.toDTO(null);

        // Then
        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("AuteurMapper - toDTO sans livres devrait retourner 0")
    void auteurMapper_toDTO_WithNoBooks_ShouldReturnZeroBooks() {
        // When
        AuteurDTO dto = auteurMapper.toDTO(auteur);

        // Then
        assertThat(dto.getNombreLivres()).isEqualTo(0);
    }

    @Test
    @DisplayName("AuteurMapper - toEntity devrait convertir AuteurDTO en Auteur")
    void auteurMapper_toEntity_ShouldConvertToEntity() {
        // Given
        AuteurDTO dto = AuteurDTO.builder()
                .id(1L)
                .nom("Hugo")
                .prenom("Victor")
                .email("victor.hugo@test.com")
                .build();

        // When
        Auteur entity = auteurMapper.toEntity(dto);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getNom()).isEqualTo("Hugo");
        assertThat(entity.getPrenom()).isEqualTo("Victor");
        assertThat(entity.getEmail()).isEqualTo("victor.hugo@test.com");
    }

    @Test
    @DisplayName("AuteurMapper - updateEntityFromDTO devrait mettre à jour l'entité")
    void auteurMapper_updateEntityFromDTO_ShouldUpdateEntity() {
        // Given
        AuteurDTO dto = AuteurDTO.builder()
                .nom("Hugo-Dupont")
                .prenom("Victor Marie")
                .email("vm.hugo@test.com")
                .build();

        // When
        auteurMapper.updateEntityFromDTO(dto, auteur);

        // Then
        assertThat(auteur.getNom()).isEqualTo("Hugo-Dupont");
        assertThat(auteur.getPrenom()).isEqualTo("Victor Marie");
        assertThat(auteur.getEmail()).isEqualTo("vm.hugo@test.com");
    }

    @Test
    @DisplayName("AuteurMapper - toDTOList devrait convertir une liste")
    void auteurMapper_toDTOList_ShouldConvertList() {
        // Given
        Auteur auteur2 = Auteur.builder()
                .id(2L)
                .nom("Camus")
                .prenom("Albert")
                .email("albert.camus@test.com")
                .livres(new ArrayList<>())
                .build();
        List<Auteur> auteurs = Arrays.asList(auteur, auteur2);

        // When
        List<AuteurDTO> dtos = auteurMapper.toDTOList(auteurs);

        // Then
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getNom()).isEqualTo("Hugo");
        assertThat(dtos.get(1).getNom()).isEqualTo("Camus");
    }

    @Test
    @DisplayName("AuteurMapper - toDTOList avec liste null devrait retourner null")
    void auteurMapper_toDTOList_WithNull_ShouldReturnNull() {
        // When
        List<AuteurDTO> dtos = auteurMapper.toDTOList(null);

        // Then
        assertThat(dtos).isNull();
    }

    @Test
    @DisplayName("LivreMapper - toDTO sans auteur ne devrait pas planter")
    void livreMapper_toDTO_WithoutAuteur_ShouldNotFail() {
        // Given
        livre.setAuteur(null);

        // When
        LivreDTO dto = livreMapper.toDTO(livre);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getAuteurId()).isNull();
        assertThat(dto.getNomCompletAuteur()).isNull();
    }
}