package com.m2i.BiblioRestApi.service;

import com.m2i.BiblioRestApi.dto.LivreDTO;
import com.m2i.BiblioRestApi.exception.DuplicateResourceException;
import com.m2i.BiblioRestApi.exception.ResourceNotFoundException;
import com.m2i.BiblioRestApi.mapper.LivreMapper;
import com.m2i.BiblioRestApi.model.Auteur;
import com.m2i.BiblioRestApi.model.Livre;
import com.m2i.BiblioRestApi.repository.AuteurRepository;
import com.m2i.BiblioRestApi.repository.LivreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - LivreService")
class LivreServiceTest {

    @Mock
    private LivreRepository livreRepository;

    @Mock
    private AuteurRepository auteurRepository;

    @Mock
    private LivreMapper livreMapper;

    @InjectMocks
    private LivreService livreService;

    private Livre livre;
    private LivreDTO livreDTO;
    private Auteur auteur;

    @BeforeEach
    void setUp() {
        auteur = Auteur.builder()
                .id(1L)
                .nom("Hugo")
                .prenom("Victor")
                .email("victor.hugo@example.com")
                .build();

        livre = Livre.builder()
                .id(1L)
                .titre("Les Misérables")
                .isbn("9782070409228")
                .anneePublication(1862)
                .nombreExemplaires(5)
                .auteur(auteur)
                .build();

        livreDTO = LivreDTO.builder()
                .id(1L)
                .titre("Les Misérables")
                .isbn("9782070409228")
                .anneePublication(1862)
                .nombreExemplaires(5)
                .auteurId(1L)
                .nomCompletAuteur("Victor Hugo")
                .build();
    }

    @Test
    @DisplayName("Devrait récupérer tous les livres")
    void getAllLivres_ShouldReturnAllBooks() {
        // Given
        List<Livre> livres = Arrays.asList(livre);
        List<LivreDTO> livreDTOs = Arrays.asList(livreDTO);

        when(livreRepository.findAll()).thenReturn(livres);
        when(livreMapper.toDTOList(livres)).thenReturn(livreDTOs);

        // When
        List<LivreDTO> result = livreService.getAllLivres();

        // Then
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).getTitre()).isEqualTo("Les Misérables");
        verify(livreRepository, times(1)).findAll();
        verify(livreMapper, times(1)).toDTOList(livres);
    }

    @Test
    @DisplayName("Devrait récupérer un livre par ID")
    void getLivreById_WhenLivreExists_ShouldReturnLivre() {
        // Given
        when(livreRepository.findById(1L)).thenReturn(Optional.of(livre));
        when(livreMapper.toDTO(livre)).thenReturn(livreDTO);

        // When
        LivreDTO result = livreService.getLivreById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitre()).isEqualTo("Les Misérables");
        verify(livreRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Devrait lever ResourceNotFoundException quand le livre n'existe pas")
    void getLivreById_WhenLivreNotExists_ShouldThrowException() {
        // Given
        when(livreRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> livreService.getLivreById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Livre non trouvé(e) avec id : '999'");

        verify(livreRepository, times(1)).findById(999L);
        verify(livreMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("Devrait récupérer un livre par ISBN")
    void getLivreByIsbn_WhenLivreExists_ShouldReturnLivre() {
        // Given
        String isbn = "9782070409228";
        when(livreRepository.findByIsbn(isbn)).thenReturn(Optional.of(livre));
        when(livreMapper.toDTO(livre)).thenReturn(livreDTO);

        // When
        LivreDTO result = livreService.getLivreByIsbn(isbn);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsbn()).isEqualTo(isbn);
        verify(livreRepository, times(1)).findByIsbn(isbn);
    }

    @Test
    @DisplayName("Devrait créer un nouveau livre")
    void createLivre_WhenValidData_ShouldCreateLivre() {
        // Given
        when(livreRepository.existsByIsbn(livreDTO.getIsbn())).thenReturn(false);
        when(auteurRepository.findById(1L)).thenReturn(Optional.of(auteur));
        when(livreMapper.toEntity(livreDTO, auteur)).thenReturn(livre);
        when(livreRepository.save(livre)).thenReturn(livre);
        when(livreMapper.toDTO(livre)).thenReturn(livreDTO);

        // When
        LivreDTO result = livreService.createLivre(livreDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitre()).isEqualTo("Les Misérables");
        verify(livreRepository, times(1)).existsByIsbn(livreDTO.getIsbn());
        verify(auteurRepository, times(1)).findById(1L);
        verify(livreRepository, times(1)).save(livre);
    }

    @Test
    @DisplayName("Devrait lever DuplicateResourceException si ISBN existe déjà")
    void createLivre_WhenIsbnExists_ShouldThrowException() {
        // Given
        when(livreRepository.existsByIsbn(livreDTO.getIsbn())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> livreService.createLivre(livreDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Livre existe déjà avec isbn");

        verify(livreRepository, times(1)).existsByIsbn(livreDTO.getIsbn());
        verify(livreRepository, never()).save(any());
    }

    @Test
    @DisplayName("Devrait lever ResourceNotFoundException si auteur n'existe pas lors de la création")
    void createLivre_WhenAuteurNotExists_ShouldThrowException() {
        // Given
        when(livreRepository.existsByIsbn(livreDTO.getIsbn())).thenReturn(false);
        when(auteurRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> livreService.createLivre(livreDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Auteur non trouvé(e) avec id : '1'");

        verify(auteurRepository, times(1)).findById(1L);
        verify(livreRepository, never()).save(any());
    }

    @Test
    @DisplayName("Devrait mettre à jour un livre")
    void updateLivre_WhenValidData_ShouldUpdateLivre() {
        // Given
        when(livreRepository.findById(1L)).thenReturn(Optional.of(livre));
        when(livreRepository.findByIsbn(livreDTO.getIsbn())).thenReturn(Optional.of(livre));
        when(auteurRepository.findById(1L)).thenReturn(Optional.of(auteur));
        when(livreRepository.save(livre)).thenReturn(livre);
        when(livreMapper.toDTO(livre)).thenReturn(livreDTO);

        // When
        LivreDTO result = livreService.updateLivre(1L, livreDTO);

        // Then
        assertThat(result).isNotNull();
        verify(livreRepository, times(1)).findById(1L);
        verify(livreMapper, times(1)).updateEntityFromDTO(livreDTO, livre, auteur);
        verify(livreRepository, times(1)).save(livre);
    }

    @Test
    @DisplayName("Devrait supprimer un livre")
    void deleteLivre_WhenLivreExists_ShouldDeleteLivre() {
        // Given
        when(livreRepository.findById(1L)).thenReturn(Optional.of(livre));
        doNothing().when(livreRepository).delete(livre);

        // When
        livreService.deleteLivre(1L);

        // Then
        verify(livreRepository, times(1)).findById(1L);
        verify(livreRepository, times(1)).delete(livre);
    }

    @Test
    @DisplayName("Devrait mettre à jour le nombre d'exemplaires")
    void updateNombreExemplaires_ShouldUpdateQuantity() {
        // Given
        when(livreRepository.findById(1L)).thenReturn(Optional.of(livre));
        when(livreRepository.save(livre)).thenReturn(livre);
        when(livreMapper.toDTO(livre)).thenReturn(livreDTO);

        // When
        LivreDTO result = livreService.updateNombreExemplaires(1L, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(livre.getNombreExemplaires()).isEqualTo(10);
        verify(livreRepository, times(1)).findById(1L);
        verify(livreRepository, times(1)).save(livre);
    }

    @Test
    @DisplayName("Devrait récupérer les livres par auteur")
    void getLivresByAuteur_ShouldReturnBooksByAuthor() {
        // Given
        List<Livre> livres = Arrays.asList(livre);
        List<LivreDTO> livreDTOs = Arrays.asList(livreDTO);

        when(auteurRepository.existsById(1L)).thenReturn(true);
        when(livreRepository.findByAuteurId(1L)).thenReturn(livres);
        when(livreMapper.toDTOList(livres)).thenReturn(livreDTOs);

        // When
        List<LivreDTO> result = livreService.getLivresByAuteur(1L);

        // Then
        assertThat(result).isNotNull().hasSize(1);
        verify(auteurRepository, times(1)).existsById(1L);
        verify(livreRepository, times(1)).findByAuteurId(1L);
    }

    @Test
    @DisplayName("Devrait rechercher des livres par titre")
    void searchLivresByTitre_ShouldReturnMatchingBooks() {
        // Given
        List<Livre> livres = Arrays.asList(livre);
        List<LivreDTO> livreDTOs = Arrays.asList(livreDTO);

        when(livreRepository.findByTitreContainingIgnoreCase("Misérables")).thenReturn(livres);
        when(livreMapper.toDTOList(livres)).thenReturn(livreDTOs);

        // When
        List<LivreDTO> result = livreService.searchLivresByTitre("Misérables");

        // Then
        assertThat(result).isNotNull().hasSize(1);
        verify(livreRepository, times(1)).findByTitreContainingIgnoreCase("Misérables");
    }

    @Test
    @DisplayName("Devrait rechercher des livres par période")
    void searchLivresByAnnee_ShouldReturnBooksInPeriod() {
        // Given
        List<Livre> livres = Arrays.asList(livre);
        List<LivreDTO> livreDTOs = Arrays.asList(livreDTO);

        when(livreRepository.findByAnneePublicationBetween(1850, 1900)).thenReturn(livres);
        when(livreMapper.toDTOList(livres)).thenReturn(livreDTOs);

        // When
        List<LivreDTO> result = livreService.searchLivresByAnnee(1850, 1900);

        // Then
        assertThat(result).isNotNull().hasSize(1);
        verify(livreRepository, times(1)).findByAnneePublicationBetween(1850, 1900);
    }
}