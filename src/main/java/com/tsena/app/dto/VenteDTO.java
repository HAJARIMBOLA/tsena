package com.tsena.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenteDTO {

    private Long id;
    private Long siteId;
    private Long produitId;
    private Long utilisateurId;
    private BigDecimal quantite;
    private BigDecimal montantTotal;
    private LocalDateTime dateVente;
}
