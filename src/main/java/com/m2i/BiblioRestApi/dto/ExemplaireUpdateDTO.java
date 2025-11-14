package com.m2i.BiblioRestApi.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExemplaireUpdateDTO {

    @NotNull(message = "Le nombre d'exemplaires est obligatoire")
    @Min(value = 0, message = "Le nombre d'exemplaires ne peut pas être négatif")
    private Integer nombreExemplaires;
}
