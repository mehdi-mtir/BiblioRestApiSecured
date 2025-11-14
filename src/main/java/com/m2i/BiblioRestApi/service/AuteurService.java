package com.m2i.BiblioRestApi.service;

import com.m2i.BiblioRestApi.dto.AuteurDTO;
import com.m2i.BiblioRestApi.dto.LivreDTO;
import com.m2i.BiblioRestApi.exception.BusinessException;
import com.m2i.BiblioRestApi.exception.DuplicateResourceException;
import com.m2i.BiblioRestApi.exception.ResourceNotFoundException;
import com.m2i.BiblioRestApi.mapper.AuteurMapper;
import com.m2i.BiblioRestApi.mapper.LivreMapper;
import com.m2i.BiblioRestApi.model.Auteur;
import com.m2i.BiblioRestApi.repository.AuteurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuteurService {

    @Autowired
    private AuteurRepository auteurRepository;

    @Autowired
    private AuteurMapper auteurMapper;

    @Autowired
    private LivreMapper livreMapper;

    /**
     * Récupère tous les auteurs
     */
    @Transactional(readOnly = true)
    public List<AuteurDTO> getAllAuteurs() {
        List<Auteur> auteurs = auteurRepository.findAll();
        return auteurMapper.toDTOList(auteurs);
    }

    /**
     * Récupère un auteur par son ID
     */
    @Transactional(readOnly = true)
    public AuteurDTO getAuteurById(Long id) {
        Auteur auteur = auteurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auteur", "id", id));
        return auteurMapper.toDTO(auteur);
    }

    /**
     * Crée un nouvel auteur
     */
    public AuteurDTO createAuteur(AuteurDTO auteurDTO) {
        // Vérifier que l'email n'existe pas déjà
        if (auteurDTO.getEmail() != null &&
                auteurRepository.existsByEmail(auteurDTO.getEmail())) {
            throw new DuplicateResourceException("Auteur", "email", auteurDTO.getEmail());
        }

        Auteur auteur = auteurMapper.toEntity(auteurDTO);
        Auteur savedAuteur = auteurRepository.save(auteur);
        return auteurMapper.toDTO(savedAuteur);
    }

    /**
     * Met à jour un auteur existant
     */
    public AuteurDTO updateAuteur(Long id, AuteurDTO auteurDTO) {
        Auteur auteur = auteurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auteur", "id", id));

        // Vérifier que l'email n'est pas déjà utilisé par un autre auteur
        if (auteurDTO.getEmail() != null) {
            auteurRepository.findByEmail(auteurDTO.getEmail())
                    .ifPresent(existingAuteur -> {
                        if (!existingAuteur.getId().equals(id)) {
                            throw new DuplicateResourceException("Auteur", "email",
                                    auteurDTO.getEmail());
                        }
                    });
        }

        auteurMapper.updateEntityFromDTO(auteurDTO, auteur);
        Auteur updatedAuteur = auteurRepository.save(auteur);
        return auteurMapper.toDTO(updatedAuteur);
    }

    /**
     * Supprime un auteur
     */
    public void deleteAuteur(Long id) {
        Auteur auteur = auteurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auteur", "id", id));

        // Vérifier que l'auteur n'a pas de livres associés
        if (!auteur.getLivres().isEmpty()) {
            throw new BusinessException(
                    "Impossible de supprimer l'auteur car il possède " +
                            auteur.getLivres().size() + " livre(s). " +
                            "Veuillez d'abord supprimer ou réassigner ses livres.");
        }

        auteurRepository.delete(auteur);
    }

    /**
     * Récupère les livres d'un auteur
     */
    @Transactional(readOnly = true)
    public List<LivreDTO> getLivresByAuteur(Long auteurId) {
        Auteur auteur = auteurRepository.findById(auteurId)
                .orElseThrow(() -> new ResourceNotFoundException("Auteur", "id", auteurId));

        return livreMapper.toDTOList(auteur.getLivres());
    }
}
