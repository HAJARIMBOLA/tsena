package com.tsena.app.dto;

import com.tsena.app.entity.Unite;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProduitDTO {

    private Long id;

    @NotBlank
    private String nom;

    @NotBlank
    private String categorie;

    @NotNull
    private Unite unite;

    private Boolean actif;
}
