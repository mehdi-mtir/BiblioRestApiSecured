package com.m2i.BiblioRestApi.mapper;

import com.m2i.BiblioRestApi.dto.LivreDTO;
import com.m2i.BiblioRestApi.model.Auteur;
import com.m2i.BiblioRestApi.model.Livre;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LivreMapper {

    /**
     * Convertit une entité Livre en LivreDTO
     */
    public LivreDTO toDTO(Livre livre) {
        if (livre == null) {
            return null;
        }

        LivreDTO dto = LivreDTO.builder()
                .id(livre.getId())
                .titre(livre.getTitre())
                .isbn(livre.getIsbn())
                .anneePublication(livre.getAnneePublication())
                .nombreExemplaires(livre.getNombreExemplaires())
                .build();

        if (livre.getAuteur() != null) {
            dto.setAuteurId(livre.getAuteur().getId());
            dto.setNomCompletAuteur(livre.getAuteur().getPrenom() + " " +
                    livre.getAuteur().getNom());
        }

        return dto;
    }

    /**
     * Convertit un LivreDTO en entité Livre
     */
    public Livre toEntity(LivreDTO dto, Auteur auteur) {
        if (dto == null) {
            return null;
        }

        return Livre.builder()
                .id(dto.getId())
                .titre(dto.getTitre())
                .isbn(dto.getIsbn())
                .anneePublication(dto.getAnneePublication())
                .nombreExemplaires(dto.getNombreExemplaires())
                .auteur(auteur)
                .build();
    }

    /**
     * Met à jour une entité Livre existante avec les données d'un DTO
     */
    public void updateEntityFromDTO(LivreDTO dto, Livre livre, Auteur auteur) {
        if (dto == null || livre == null) {
            return;
        }

        livre.setTitre(dto.getTitre());
        livre.setIsbn(dto.getIsbn());
        livre.setAnneePublication(dto.getAnneePublication());
        livre.setNombreExemplaires(dto.getNombreExemplaires());

        if (auteur != null) {
            livre.setAuteur(auteur);
        }
    }

    /**
     * Convertit une liste d'entités en liste de DTOs
     */
    public List<LivreDTO> toDTOList(List<Livre> livres) {
        if (livres == null) {
            return null;
        }

        return livres.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}