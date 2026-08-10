package com.tsena.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VenteCreationDTO {

    @NotNull
    private Long siteId;

    @NotNull
    private Long produitId;

    @NotNull
    @Positive
    private BigDecimal quantite;
}
