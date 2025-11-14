package com.m2i.BiblioRestApi.controller;

import com.m2i.BiblioRestApi.dto.AuteurDTO;
import com.m2i.BiblioRestApi.dto.LivreDTO;
import com.m2i.BiblioRestApi.service.AuteurService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des auteurs
 */
@RestController
@RequestMapping("/api/auteurs")
public class AuteurController {

    @Autowired
    private AuteurService auteurService;

    /**
     * GET /api/auteurs - Récupérer tous les auteurs
     *
     * @return Liste de tous les auteurs
     */
    @GetMapping
    public ResponseEntity<List<AuteurDTO>> getAllAuteurs() {
        List<AuteurDTO> auteurs = auteurService.getAllAuteurs();
        return ResponseEntity.ok(auteurs);
    }

    /**
     * GET /api/auteurs/{id} - Récupérer un auteur par son ID
     *
     * @param id L'identifiant de l'auteur
     * @return L'auteur trouvé
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuteurDTO> getAuteurById(@PathVariable Long id) {
        AuteurDTO auteur = auteurService.getAuteurById(id);
        return ResponseEntity.ok(auteur);
    }

    /**
     * POST /api/auteurs - Créer un nouvel auteur
     *
     * @param auteurDTO Les données de l'auteur à créer
     * @return L'auteur créé avec le statut 201 Created
     */
    @PostMapping
    public ResponseEntity<AuteurDTO> createAuteur(@Valid @RequestBody AuteurDTO auteurDTO) {
        AuteurDTO createdAuteur = auteurService.createAuteur(auteurDTO);

        // Créer l'URI de la ressource créée
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdAuteur.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdAuteur);
    }

    /**
     * PUT /api/auteurs/{id} - Mettre à jour un auteur
     *
     * @param id L'identifiant de l'auteur à modifier
     * @param auteurDTO Les nouvelles données de l'auteur
     * @return L'auteur mis à jour
     */
    @PutMapping("/{id}")
    public ResponseEntity<AuteurDTO> updateAuteur(
            @PathVariable Long id,
            @Valid @RequestBody AuteurDTO auteurDTO) {
        AuteurDTO updatedAuteur = auteurService.updateAuteur(id, auteurDTO);
        return ResponseEntity.ok(updatedAuteur);
    }

    /**
     * DELETE /api/auteurs/{id} - Supprimer un auteur
     *
     * @param id L'identifiant de l'auteur à supprimer
     * @return Statut 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuteur(@PathVariable Long id) {
        auteurService.deleteAuteur(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/auteurs/{id}/livres - Récupérer les livres d'un auteur
     *
     * @param id L'identifiant de l'auteur
     * @return Liste des livres de l'auteur
     */
    @GetMapping("/{id}/livres")
    public ResponseEntity<List<LivreDTO>> getLivresByAuteur(@PathVariable Long id) {
        List<LivreDTO> livres = auteurService.getLivresByAuteur(id);
        return ResponseEntity.ok(livres);
    }
}