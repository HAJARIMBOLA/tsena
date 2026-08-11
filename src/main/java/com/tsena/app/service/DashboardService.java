package com.tsena.app.service;

import com.tsena.app.dto.DashboardGlobalDTO;
import com.tsena.app.dto.DashboardSiteDTO;
import com.tsena.app.dto.PointEvolutionDTO;
import com.tsena.app.exception.ResourceNotFoundException;
import com.tsena.app.repository.SiteRepository;
import com.tsena.app.repository.VenteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final VenteRepository venteRepository;
    private final SiteRepository siteRepository;

    public DashboardService(VenteRepository venteRepository, SiteRepository siteRepository) {
        this.venteRepository = venteRepository;
        this.siteRepository = siteRepository;
    }

    public DashboardSiteDTO dashboardParSite(Long siteId, String periode) {
        if (!siteRepository.existsById(siteId)) {
            throw new ResourceNotFoundException("Site introuvable : " + siteId);
        }

        LocalDateTime debut = debutPeriode(periode);
        LocalDateTime fin = LocalDateTime.now();

        return DashboardSiteDTO.builder()
                .chiffreAffairesTotal(venteRepository.sommeMontantParSite(siteId, debut, fin))
                .nombreVentes(venteRepository.compterParSite(siteId, debut, fin))
                .quantiteVenduePoidsKg(venteRepository.sommeQuantitePoidsKgParSite(siteId, debut, fin))
                .quantiteVendueSacs(venteRepository.sommeQuantiteSacsParSite(siteId, debut, fin))
                .topProduits(venteRepository.topProduitsParSite(siteId, debut, fin, PageRequest.of(0, 5)))
                .evolution(mapperEvolution(venteRepository.evolutionParSite(siteId, debut, fin)))
                .build();
    }

    public DashboardGlobalDTO dashboardGlobal(String periode) {
        LocalDateTime debut = debutPeriode(periode);
        LocalDateTime fin = LocalDateTime.now();

        return DashboardGlobalDTO.builder()
                .chiffreAffairesTotal(venteRepository.sommeMontantGlobal(debut, fin))
                .nombreVentes(venteRepository.compterGlobal(debut, fin))
                .quantiteVenduePoidsKg(venteRepository.sommeQuantitePoidsKgGlobal(debut, fin))
                .quantiteVendueSacs(venteRepository.sommeQuantiteSacsGlobal(debut, fin))
                .topProduits(venteRepository.topProduitsGlobal(debut, fin, PageRequest.of(0, 5)))
                .evolution(mapperEvolution(venteRepository.evolutionGlobale(debut, fin)))
                .classementSites(venteRepository.classementSites(debut, fin))
                .build();
    }

    private LocalDateTime debutPeriode(String periode) {
        LocalDateTime maintenant = LocalDateTime.now();
        String cle = periode == null ? "jour" : periode.toLowerCase();

        return switch (cle) {
            case "semaine" -> maintenant.toLocalDate()
                    .minusDays(maintenant.getDayOfWeek().getValue() - 1L)
                    .atStartOfDay();
            case "mois" -> maintenant.toLocalDate().withDayOfMonth(1).atStartOfDay();
            default -> maintenant.toLocalDate().atStartOfDay();
        };
    }

    private List<PointEvolutionDTO> mapperEvolution(List<Object[]> lignes) {
        return lignes.stream()
                .map(ligne -> new PointEvolutionDTO(
                        ((LocalDateTime) ligne[0]).toLocalDate(),
                        (BigDecimal) ligne[1]))
                .toList();
    }
}
