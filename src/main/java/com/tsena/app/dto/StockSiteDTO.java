package com.tsena.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
public class StockSiteDTO {

    private Long id;

    @NotNull
    private Long siteId;

    private String siteNom;

    @NotNull
    private Long produitId;

    @NotNull
    @PositiveOrZero
    private BigDecimal quantiteDisponible;
}
