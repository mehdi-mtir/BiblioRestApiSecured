package com.m2i.BiblioRestApi.service;

import com.m2i.BiblioRestApi.dto.AuteurDTO;
import com.m2i.BiblioRestApi.dto.LivreDTO;
import com.m2i.BiblioRestApi.exception.BusinessException;
import com.m2i.BiblioRestApi.exception.DuplicateResourceException;
import com.m2i.BiblioRestApi.exception.ResourceNotFoundException;
import com.m2i.BiblioRestApi.mapper.AuteurMapper;
import com.m2i.BiblioRestApi.mapper.LivreMapper;
import com.m2i.BiblioRestApi.model.Auteur;
import com.m2i.BiblioRestApi.model.Livre;
import com.m2i.BiblioRestApi.repository.AuteurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - AuteurService")
class AuteurServiceTest {

    @Mock
    private AuteurRepository auteurRepository;

    @Mock
    private AuteurMapper auteurMapper;

    @Mock
    private LivreMapper livreMapper;

    @InjectMocks
    private AuteurService auteurService;

    private Auteur auteur;
    private AuteurDTO auteurDTO;

    @BeforeEach
    void setUp() {
        auteur = Auteur.builder()
                .id(1L)
                .nom("Hugo")
                .prenom("Victor")
                .email("victor.hugo@example.com")
                .livres(new ArrayList<>())
                .build();

        auteurDTO = AuteurDTO.builder()
                .id(1L)
                .nom("Hugo")
                .prenom("Victor")
                .email("victor.hugo@example.com")
                .nombreLivres(0)
                .build();
    }

    @Test
    @DisplayName("Devrait récupérer tous les auteurs")
    void getAllAuteurs_ShouldReturnAllAuthors() {
        // Given
        List<Auteur> auteurs = Arrays.asList(auteur);
        List<AuteurDTO> auteurDTOs = Arrays.asList(auteurDTO);

        when(auteurRepository.findAll()).thenReturn(auteurs);
        when(auteurMapper.toDTOList(auteurs)).thenReturn(auteurDTOs);

        // When
        List<AuteurDTO> result = auteurService.getAllAuteurs();

        // Then
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).getNom()).isEqualTo("Hugo");
        verify(auteurRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Devrait récupérer un auteur par ID")
    void getAuteurById_WhenAuteurExists_ShouldReturnAuteur() {
        // Given
        when(auteurRepository.findById(1L)).thenReturn(Optional.of(auteur));
        when(auteurMapper.toDTO(auteur)).thenReturn(auteurDTO);

        // When
        AuteurDTO result = auteurService.getAuteurById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNom()).isEqualTo("Hugo");
        assertThat(result.getPrenom()).isEqualTo("Victor");
        verify(auteurRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Devrait lever ResourceNotFoundException quand l'auteur n'existe pas")
    void getAuteurById_WhenAuteurNotExists_ShouldThrowException() {
        // Given
        when(auteurRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> auteurService.getAuteurById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Auteur non trouvé(e) avec id : '999'");

        verify(auteurRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Devrait créer un nouvel auteur")
    void createAuteur_WhenValidData_ShouldCreateAuteur() {
        // Given
        when(auteurRepository.existsByEmail(auteurDTO.getEmail())).thenReturn(false);
        when(auteurMapper.toEntity(auteurDTO)).thenReturn(auteur);
        when(auteurRepository.save(auteur)).thenReturn(auteur);
        when(auteurMapper.toDTO(auteur)).thenReturn(auteurDTO);

        // When
        AuteurDTO result = auteurService.createAuteur(auteurDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNom()).isEqualTo("Hugo");
        verify(auteurRepository, times(1)).existsByEmail(auteurDTO.getEmail());
        verify(auteurRepository, times(1)).save(auteur);
    }

    @Test
    @DisplayName("Devrait lever DuplicateResourceException si email existe déjà")
    void createAuteur_WhenEmailExists_ShouldThrowException() {
        // Given
        when(auteurRepository.existsByEmail(auteurDTO.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> auteurService.createAuteur(auteurDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Auteur existe déjà avec email");

        verify(auteurRepository, times(1)).existsByEmail(auteurDTO.getEmail());
        verify(auteurRepository, never()).save(any());
    }

    @Test
    @DisplayName("Devrait mettre à jour un auteur")
    void updateAuteur_WhenValidData_ShouldUpdateAuteur() {
        // Given
        when(auteurRepository.findById(1L)).thenReturn(Optional.of(auteur));
        when(auteurRepository.findByEmail(auteurDTO.getEmail())).thenReturn(Optional.of(auteur));
        when(auteurRepository.save(auteur)).thenReturn(auteur);
        when(auteurMapper.toDTO(auteur)).thenReturn(auteurDTO);

        // When
        AuteurDTO result = auteurService.updateAuteur(1L, auteurDTO);

        // Then
        assertThat(result).isNotNull();
        verify(auteurRepository, times(1)).findById(1L);
        verify(auteurMapper, times(1)).updateEntityFromDTO(auteurDTO, auteur);
        verify(auteurRepository, times(1)).save(auteur);
    }

    @Test
    @DisplayName("Devrait lever exception si email utilisé par un autre auteur lors de la mise à jour")
    void updateAuteur_WhenEmailUsedByOther_ShouldThrowException() {
        // Given
        Auteur autreAuteur = Auteur.builder().id(2L).email("victor.hugo@example.com").build();

        when(auteurRepository.findById(1L)).thenReturn(Optional.of(auteur));
        when(auteurRepository.findByEmail(auteurDTO.getEmail())).thenReturn(Optional.of(autreAuteur));

        // When & Then
        assertThatThrownBy(() -> auteurService.updateAuteur(1L, auteurDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Auteur existe déjà avec email");

        verify(auteurRepository, never()).save(any());
    }

    @Test
    @DisplayName("Devrait supprimer un auteur sans livres")
    void deleteAuteur_WhenNoBooks_ShouldDeleteAuteur() {
        // Given
        when(auteurRepository.findById(1L)).thenReturn(Optional.of(auteur));
        doNothing().when(auteurRepository).delete(auteur);

        // When
        auteurService.deleteAuteur(1L);

        // Then
        verify(auteurRepository, times(1)).findById(1L);
        verify(auteurRepository, times(1)).delete(auteur);
    }

    @Test
    @DisplayName("Devrait lever BusinessException si l'auteur a des livres")
    void deleteAuteur_WhenHasBooks_ShouldThrowException() {
        // Given
        Livre livre = Livre.builder().id(1L).titre("Les Misérables").build();
        auteur.getLivres().add(livre);

        when(auteurRepository.findById(1L)).thenReturn(Optional.of(auteur));

        // When & Then
        assertThatThrownBy(() -> auteurService.deleteAuteur(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Impossible de supprimer l'auteur car il possède 1 livre(s)");

        verify(auteurRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Devrait récupérer les livres d'un auteur")
    void getLivresByAuteur_ShouldReturnAuthorBooks() {
        // Given
        Livre livre = Livre.builder()
                .id(1L)
                .titre("Les Misérables")
                .isbn("9782070409228")
                .build();
        auteur.getLivres().add(livre);

        LivreDTO livreDTO = LivreDTO.builder()
                .id(1L)
                .titre("Les Misérables")
                .build();

        when(auteurRepository.findById(1L)).thenReturn(Optional.of(auteur));
        when(livreMapper.toDTOList(auteur.getLivres())).thenReturn(Arrays.asList(livreDTO));

        // When
        List<LivreDTO> result = auteurService.getLivresByAuteur(1L);

        // Then
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).getTitre()).isEqualTo("Les Misérables");
        verify(auteurRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Devrait lever exception si auteur n'existe pas lors de la récupération des livres")
    void getLivresByAuteur_WhenAuteurNotExists_ShouldThrowException() {
        // Given
        when(auteurRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> auteurService.getLivresByAuteur(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Auteur non trouvé(e) avec id : '999'");
    }
}
