package com.m2i.BiblioRestApi.mapper;

import com.m2i.BiblioRestApi.dto.AuteurDTO;
import com.m2i.BiblioRestApi.model.Auteur;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuteurMapper {

    /**
     * Convertit une entité Auteur en AuteurDTO
     */
    public AuteurDTO toDTO(Auteur auteur) {
        if (auteur == null) {
            return null;
        }

        return AuteurDTO.builder()
                .id(auteur.getId())
                .nom(auteur.getNom())
                .prenom(auteur.getPrenom())
                .email(auteur.getEmail())
                .nombreLivres(auteur.getLivres() != null ? auteur.getLivres().size() : 0)
                .build();
    }

    /**
     * Convertit un AuteurDTO en entité Auteur
     */
    public Auteur toEntity(AuteurDTO dto) {
        if (dto == null) {
            return null;
        }

        return Auteur.builder()
                .id(dto.getId())
                .nom(dto.getNom())
                .prenom(dto.getPrenom())
                .email(dto.getEmail())
                .build();
    }

    /**
     * Met à jour une entité Auteur existante avec les données d'un DTO
     */
    public void updateEntityFromDTO(AuteurDTO dto, Auteur auteur) {
        if (dto == null || auteur == null) {
            return;
        }

        auteur.setNom(dto.getNom());
        auteur.setPrenom(dto.getPrenom());
        auteur.setEmail(dto.getEmail());
    }

    /**
     * Convertit une liste d'entités en liste de DTOs
     */
    public List<AuteurDTO> toDTOList(List<Auteur> auteurs) {
        if (auteurs == null) {
            return null;
        }

        return auteurs.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}