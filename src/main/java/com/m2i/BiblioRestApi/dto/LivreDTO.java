package com.m2i.BiblioRestApi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LivreDTO {

    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 1, max = 200, message = "Le titre doit contenir entre 1 et 200 caractères")
    private String titre;

    @NotBlank(message = "L'ISBN est obligatoire")
    @Pattern(regexp = "^(978|979)[0-9]{10}$", message = "Format ISBN-13 invalide")
    private String isbn;

    @NotNull(message = "L'année de publication est obligatoire")
    @Min(value = 1000, message = "L'année doit être supérieure à 1000")
    @Max(value = 2100, message = "L'année doit être inférieure à 2100")
    private Integer anneePublication;

    @Min(value = 0, message = "Le nombre d'exemplaires ne peut pas être négatif")
    private Integer nombreExemplaires;

    @NotNull(message = "L'ID de l'auteur est obligatoire")
    private Long auteurId;

    // Pour la lecture seulement (ignoré lors de la création/mise à jour)
    private String nomCompletAuteur;
}