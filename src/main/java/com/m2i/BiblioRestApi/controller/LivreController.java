package com.m2i.BiblioRestApi.controller;

import com.m2i.BiblioRestApi.dto.ExemplaireUpdateDTO;
import com.m2i.BiblioRestApi.dto.LivreDTO;
import com.m2i.BiblioRestApi.service.LivreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des livres
 */
@RestController
@RequestMapping("/api/livres")
public class LivreController {

    @Autowired
    private LivreService livreService;

    /**
     * GET /api/livres - Liste tous les livres
     *
     * @return Liste de tous les livres
     */
    @GetMapping
    public ResponseEntity<List<LivreDTO>> getAllLivres() {
        List<LivreDTO> livres = livreService.getAllLivres();
        return ResponseEntity.ok(livres);
    }

    /**
     * GET /api/livres/{id} - Récupère un livre par ID
     *
     * @param id L'identifiant du livre
     * @return Le livre trouvé
     */
    @GetMapping("/{id}")
    public ResponseEntity<LivreDTO> getLivreById(@PathVariable Long id) {
        LivreDTO livre = livreService.getLivreById(id);
        return ResponseEntity.ok(livre);
    }

    /**
     * GET /api/livres/isbn/{isbn} - Recherche par ISBN
     *
     * @param isbn L'ISBN du livre
     * @return Le livre trouvé
     */
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<LivreDTO> getLivreByIsbn(@PathVariable String isbn) {
        LivreDTO livre = livreService.getLivreByIsbn(isbn);
        return ResponseEntity.ok(livre);
    }

    @GetMapping("/auteur/{id}")
    public ResponseEntity<List<LivreDTO>> getLivresByAuteur(@PathVariable Long id) {
        List<LivreDTO> livres = livreService.getLivresByAuteur(id);
        return ResponseEntity.ok(livres);
    }

    /**
     * GET /api/livres/search - Recherche des livres par critères
     *
     * @param titre Titre du livre (recherche partielle, optionnel)
     * @param anneeMin Année minimum de publication (optionnel)
     * @param anneeMax Année maximum de publication (optionnel)
     * @return Liste des livres correspondant aux critères
     */
    @GetMapping("/search")
    public ResponseEntity<List<LivreDTO>> searchLivres(
            @RequestParam(required = false) String titre,
            @RequestParam(required = false) Integer anneeMin,
            @RequestParam(required = false) Integer anneeMax) {

        List<LivreDTO> livres;

        if (titre != null && !titre.isEmpty()) {
            livres = livreService.searchLivresByTitre(titre);
        } else if (anneeMin != null && anneeMax != null) {
            livres = livreService.searchLivresByAnnee(anneeMin, anneeMax);
        } else {
            livres = livreService.getAllLivres();
        }

        return ResponseEntity.ok(livres);
    }

    /**
     * POST /api/livres - Crée un nouveau livre
     *
     * @param livreDTO Les données du livre à créer
     * @return Le livre créé avec le statut 201 Created
     */
    @PostMapping
    public ResponseEntity<LivreDTO> createLivre(@Valid @RequestBody LivreDTO livreDTO) {
        LivreDTO createdLivre = livreService.createLivre(livreDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdLivre.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdLivre);
    }

    /**
     * PUT /api/livres/{id} - Met à jour un livre
     *
     * @param id L'identifiant du livre à modifier
     * @param livreDTO Les nouvelles données du livre
     * @return Le livre mis à jour
     */
    @PutMapping("/{id}")
    public ResponseEntity<LivreDTO> updateLivre(
            @PathVariable Long id,
            @Valid @RequestBody LivreDTO livreDTO) {
        LivreDTO updatedLivre = livreService.updateLivre(id, livreDTO);
        return ResponseEntity.ok(updatedLivre);
    }

    /**
     * PATCH /api/livres/{id}/exemplaires - Met à jour le nombre d'exemplaires
     *
     * @param id L'identifiant du livre
     * @param updateDTO L'objet contenant le nouveau nombre d'exemplaires
     * @return Le livre mis à jour
     */
    @PatchMapping("/{id}/exemplaires")
    public ResponseEntity<LivreDTO> updateNombreExemplaires(
            @PathVariable Long id,
            @Valid @RequestBody ExemplaireUpdateDTO updateDTO) {
        LivreDTO updatedLivre = livreService.updateNombreExemplaires(
                id, updateDTO.getNombreExemplaires());
        return ResponseEntity.ok(updatedLivre);
    }

    /**
     * DELETE /api/livres/{id} - Supprime un livre
     *
     * @param id L'identifiant du livre à supprimer
     * @return Statut 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLivre(@PathVariable Long id) {
        livreService.deleteLivre(id);
        return ResponseEntity.noContent().build();
    }


}
