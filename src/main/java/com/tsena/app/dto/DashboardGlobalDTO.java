package com.tsena.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardGlobalDTO {

    private BigDecimal chiffreAffairesTotal;
    private Long nombreVentes;
    private BigDecimal quantiteVenduePoidsKg;
    private BigDecimal quantiteVendueSacs;
    private List<VenteAgregeeDTO> topProduits;
    private List<PointEvolutionDTO> evolution;
    private List<SiteClassementDTO> classementSites;
}
