package com.m2i.BiblioRestApi.service;

import com.m2i.BiblioRestApi.dto.LivreDTO;
import com.m2i.BiblioRestApi.exception.DuplicateResourceException;
import com.m2i.BiblioRestApi.exception.ResourceNotFoundException;
import com.m2i.BiblioRestApi.mapper.LivreMapper;
import com.m2i.BiblioRestApi.model.Auteur;
import com.m2i.BiblioRestApi.model.Livre;
import com.m2i.BiblioRestApi.repository.AuteurRepository;
import com.m2i.BiblioRestApi.repository.LivreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LivreService {

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private AuteurRepository auteurRepository;

    @Autowired
    private LivreMapper livreMapper;

    /**
     * Récupère tous les livres
     */
    @Transactional(readOnly = true)
    public List<LivreDTO> getAllLivres() {
        List<Livre> livres = livreRepository.findAll();
        return livreMapper.toDTOList(livres);
    }

    /**
     * Récupère un livre par son ID
     */
    @Transactional(readOnly = true)
    public LivreDTO getLivreById(Long id) {
        Livre livre = livreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livre", "id", id));
        return livreMapper.toDTO(livre);
    }

    /**
     * Récupère un livre par son ISBN
     */
    @Transactional(readOnly = true)
    public LivreDTO getLivreByIsbn(String isbn) {
        Livre livre = livreRepository.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Livre", "isbn", isbn));
        return livreMapper.toDTO(livre);
    }

    /**
     * Crée un nouveau livre
     */
    public LivreDTO createLivre(LivreDTO livreDTO) {
        // Vérifier que l'ISBN n'existe pas déjà
        if (livreRepository.existsByIsbn(livreDTO.getIsbn())) {
            throw new DuplicateResourceException("Livre", "isbn", livreDTO.getIsbn());
        }
        // Vérifier que l'auteur existe
        Auteur auteur = auteurRepository.findById(livreDTO.getAuteurId())
                .orElseThrow(() -> new ResourceNotFoundException("Auteur", "id",
                        livreDTO.getAuteurId()));

        Livre livre = livreMapper.toEntity(livreDTO, auteur);
        Livre savedLivre = livreRepository.save(livre);
        return livreMapper.toDTO(savedLivre);
    }

    /**
     * Met à jour un livre existant
     */
    public LivreDTO updateLivre(Long id, LivreDTO livreDTO) {
        Livre livre = livreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livre", "id", id));

        // Vérifier que l'ISBN n'est pas déjà utilisé par un autre livre
        livreRepository.findByIsbn(livreDTO.getIsbn())
                .ifPresent(existingLivre -> {
                    if (!existingLivre.getId().equals(id)) {
                        throw new DuplicateResourceException("Livre", "isbn",
                                livreDTO.getIsbn());
                    }
                });

        // Vérifier que l'auteur existe
        Auteur auteur = auteurRepository.findById(livreDTO.getAuteurId())
                .orElseThrow(() -> new ResourceNotFoundException("Auteur", "id",
                        livreDTO.getAuteurId()));

        livreMapper.updateEntityFromDTO(livreDTO, livre, auteur);
        Livre updatedLivre = livreRepository.save(livre);
        return livreMapper.toDTO(updatedLivre);
    }

    /**
     * Supprime un livre
     */
    public void deleteLivre(Long id) {
        Livre livre = livreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livre", "id", id));

        livreRepository.delete(livre);
    }

    /**
     * Met à jour le nombre d'exemplaires d'un livre
     */
    public LivreDTO updateNombreExemplaires(Long id, Integer nombreExemplaires) {
        Livre livre = livreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livre", "id", id));

        livre.setNombreExemplaires(nombreExemplaires);
        Livre updatedLivre = livreRepository.save(livre);
        return livreMapper.toDTO(updatedLivre);
    }

    /**
     * Récupère les livres d'un auteur
     */
    @Transactional(readOnly = true)
    public List<LivreDTO> getLivresByAuteur(Long auteurId) {
        // Vérifier que l'auteur existe
        if (!auteurRepository.existsById(auteurId)) {
            throw new ResourceNotFoundException("Auteur", "id", auteurId);
        }

        List<Livre> livres = livreRepository.findByAuteurId(auteurId);
        return livreMapper.toDTOList(livres);
    }

    /**
     * Recherche des livres par titre (contient, insensible à la casse)
     */
    @Transactional(readOnly = true)
    public List<LivreDTO> searchLivresByTitre(String titre) {
        List<Livre> livres = livreRepository.findByTitreContainingIgnoreCase(titre);
        return livreMapper.toDTOList(livres);
    }

    /**
     * Recherche des livres par année de publication
     */
    @Transactional(readOnly = true)
    public List<LivreDTO> searchLivresByAnnee(Integer anneeMin, Integer anneeMax) {
        List<Livre> livres = livreRepository.findByAnneePublicationBetween(anneeMin, anneeMax);
        return livreMapper.toDTOList(livres);
    }
}