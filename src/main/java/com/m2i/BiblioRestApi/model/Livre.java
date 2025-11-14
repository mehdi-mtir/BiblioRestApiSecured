package com.m2i.BiblioRestApi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;


@Entity
@Table(name = "livres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Livre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 1, max = 200, message = "Le titre doit contenir entre 1 et 200 caractères")
    @Column(nullable = false, length = 200)
    private String titre;

    @NotBlank(message = "L'ISBN est obligatoire")
    @Pattern(regexp = "^(978|979)[0-9]{10}$", message = "Format ISBN-13 invalide (ex: 9782070409228)")
    @Column(unique = true, nullable = false, length = 13)
    private String isbn;

    @NotNull(message = "L'année de publication est obligatoire")
    @Min(value = 1000, message = "L'année doit être supérieure à 1000")
    @Max(value = 2100, message = "L'année doit être inférieure à 2100")
    @Column(nullable = false)
    private Integer anneePublication;

    @Min(value = 0, message = "Le nombre d'exemplaires ne peut pas être négatif")
    @Column(nullable = false)
    private Integer nombreExemplaires = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auteur_id", nullable = false)
    @NotNull(message = "L'auteur est obligatoire")
    private Auteur auteur;
}
