package com.tsena.app.service;

import com.tsena.app.dto.StockSiteDTO;
import com.tsena.app.entity.Produit;
import com.tsena.app.entity.Site;
import com.tsena.app.entity.StockSite;
import com.tsena.app.exception.ConflitException;
import com.tsena.app.exception.ResourceNotFoundException;
import com.tsena.app.repository.ProduitRepository;
import com.tsena.app.repository.SiteRepository;
import com.tsena.app.repository.StockSiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class StockSiteService {

    private final StockSiteRepository stockSiteRepository;
    private final SiteRepository siteRepository;
    private final ProduitRepository produitRepository;

    public StockSiteService(StockSiteRepository stockSiteRepository,
                             SiteRepository siteRepository,
                             ProduitRepository produitRepository) {
        this.stockSiteRepository = stockSiteRepository;
        this.siteRepository = siteRepository;
        this.produitRepository = produitRepository;
    }

    public StockSiteDTO affecter(StockSiteDTO dto) {
        if (stockSiteRepository.findBySiteIdAndProduitId(dto.getSiteId(), dto.getProduitId()).isPresent()) {
            throw new ConflitException("Un stock existe deja pour ce site et ce produit.");
        }

        Site site = siteRepository.findById(dto.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site introuvable : " + dto.getSiteId()));
        Produit produit = produitRepository.findById(dto.getProduitId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + dto.getProduitId()));

        StockSite stockSite = StockSite.builder()
                .site(site)
                .produit(produit)
                .quantiteDisponible(dto.getQuantiteDisponible())
                .prixUnitaire(dto.getPrixUnitaire())
                .build();

        return toDto(stockSiteRepository.save(stockSite));
    }

    public StockSiteDTO reapprovisionner(Long siteId, Long produitId, BigDecimal quantite) {
        StockSite stockSite = stockSiteRepository.findBySiteIdAndProduitId(siteId, produitId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucun stock trouve pour le site " + siteId + " et le produit " + produitId));

        stockSite.setQuantiteDisponible(stockSite.getQuantiteDisponible().add(quantite));
        return toDto(stockSite);
    }

    public StockSiteDTO modifierPrix(Long siteId, Long produitId, BigDecimal nouveauPrix) {
        StockSite stockSite = stockSiteRepository.findBySiteIdAndProduitId(siteId, produitId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucun stock trouve pour le site " + siteId + " et le produit " + produitId));

        stockSite.setPrixUnitaire(nouveauPrix);
        return toDto(stockSite);
    }

    @Transactional(readOnly = true)
    public List<StockSiteDTO> listerParSite(Long siteId) {
        if (!siteRepository.existsById(siteId)) {
            throw new ResourceNotFoundException("Site introuvable : " + siteId);
        }
        return stockSiteRepository.findBySiteId(siteId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockSiteDTO> listerTout() {
        return stockSiteRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    private StockSiteDTO toDto(StockSite stockSite) {
        return StockSiteDTO.builder()
                .id(stockSite.getId())
                .siteId(stockSite.getSite().getId())
                .siteNom(stockSite.getSite().getNom())
                .produitId(stockSite.getProduit().getId())
                .quantiteDisponible(stockSite.getQuantiteDisponible())
                .prixUnitaire(stockSite.getPrixUnitaire())
                .build();
    }
}
