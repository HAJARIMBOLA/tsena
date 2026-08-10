package com.tsena.app.dto;

import com.tsena.app.entity.Unite;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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

    @NotNull
    @Positive
    private BigDecimal prixUnitaire;

    private Boolean actif;
}
