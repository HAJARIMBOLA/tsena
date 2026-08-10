package com.tsena.app.service;

import com.tsena.app.dto.VenteCreationDTO;
import com.tsena.app.dto.VenteDTO;
import com.tsena.app.entity.Produit;
import com.tsena.app.entity.Site;
import com.tsena.app.entity.StockSite;
import com.tsena.app.entity.Utilisateur;
import com.tsena.app.entity.Vente;
import com.tsena.app.exception.ResourceNotFoundException;
import com.tsena.app.exception.RessourceInactiveException;
import com.tsena.app.exception.StockInsuffisantException;
import com.tsena.app.repository.ProduitRepository;
import com.tsena.app.repository.SiteRepository;
import com.tsena.app.repository.StockSiteRepository;
import com.tsena.app.repository.UtilisateurRepository;
import com.tsena.app.repository.VenteRepository;
import com.tsena.app.security.SiteAccessService;
import com.tsena.app.security.UtilisateurPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class VenteService {

    private final VenteRepository venteRepository;
    private final StockSiteRepository stockSiteRepository;
    private final SiteRepository siteRepository;
    private final ProduitRepository produitRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SiteAccessService siteAccessService;

    public VenteService(VenteRepository venteRepository,
                         StockSiteRepository stockSiteRepository,
                         SiteRepository siteRepository,
                         ProduitRepository produitRepository,
                         UtilisateurRepository utilisateurRepository,
                         SiteAccessService siteAccessService) {
        this.venteRepository = venteRepository;
        this.stockSiteRepository = stockSiteRepository;
        this.siteRepository = siteRepository;
        this.produitRepository = produitRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.siteAccessService = siteAccessService;
    }

    public VenteDTO creerVente(VenteCreationDTO dto, Authentication authentication) {
        if (!siteAccessService.aAcces(dto.getSiteId(), authentication)) {
            throw new AccessDeniedException("Acces refuse au site " + dto.getSiteId());
        }

        Site site = siteRepository.findById(dto.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site introuvable : " + dto.getSiteId()));
        if (!Boolean.TRUE.equals(site.getActif())) {
            throw new RessourceInactiveException("Le site '" + site.getNom() + "' est inactif.");
        }

        Produit produit = produitRepository.findById(dto.getProduitId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + dto.getProduitId()));
        if (!Boolean.TRUE.equals(produit.getActif())) {
            throw new RessourceInactiveException("Le produit '" + produit.getNom() + "' est inactif.");
        }

        StockSite stock = stockSiteRepository.findBySiteIdAndProduitId(dto.getSiteId(), dto.getProduitId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucun stock trouve pour le site " + dto.getSiteId() + " et le produit " + dto.getProduitId()));

        if (stock.getQuantiteDisponible().compareTo(dto.getQuantite()) < 0) {
            throw new StockInsuffisantException(
                    "Stock insuffisant pour '" + produit.getNom() + "' : disponible " + stock.getQuantiteDisponible()
                            + ", demande " + dto.getQuantite());
        }

        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateurConnecte(authentication))
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur connecte introuvable"));

        BigDecimal montantTotal = dto.getQuantite().multiply(produit.getPrixUnitaire());

        stock.setQuantiteDisponible(stock.getQuantiteDisponible().subtract(dto.getQuantite()));

        Vente vente = Vente.builder()
                .site(site)
                .produit(produit)
                .utilisateur(utilisateur)
                .quantite(dto.getQuantite())
                .montantTotal(montantTotal)
                .dateVente(LocalDateTime.now())
                .build();

        return toDto(venteRepository.save(vente));
    }

    @Transactional(readOnly = true)
    public Page<VenteDTO> historiqueParSite(Long siteId, LocalDateTime debut, LocalDateTime fin,
                                             Pageable pageable, Authentication authentication) {
        if (!siteAccessService.aAcces(siteId, authentication)) {
            throw new AccessDeniedException("Acces refuse au site " + siteId);
        }

        Page<Vente> page = (debut != null && fin != null)
                ? venteRepository.findBySiteIdAndDateVenteBetween(siteId, debut, fin, pageable)
                : venteRepository.findBySiteId(siteId, pageable);

        return page.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<VenteDTO> mesVentes(Authentication authentication, Pageable pageable) {
        Long utilisateurId = idUtilisateurConnecte(authentication);
        return venteRepository.findByUtilisateurId(utilisateurId, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public VenteDTO trouverParId(Long id, Authentication authentication) {
        Vente vente = venteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vente introuvable : " + id));

        if (!siteAccessService.aAcces(vente.getSite().getId(), authentication)) {
            throw new AccessDeniedException("Acces refuse a cette vente");
        }

        return toDto(vente);
    }

    private Long idUtilisateurConnecte(Authentication authentication) {
        if (authentication.getPrincipal() instanceof UtilisateurPrincipal principal) {
            return principal.getId();
        }
        throw new AccessDeniedException("Utilisateur non authentifie");
    }

    VenteDTO toDto(Vente vente) {
        return VenteDTO.builder()
                .id(vente.getId())
                .siteId(vente.getSite().getId())
                .produitId(vente.getProduit().getId())
                .utilisateurId(vente.getUtilisateur().getId())
                .quantite(vente.getQuantite())
                .montantTotal(vente.getMontantTotal())
                .dateVente(vente.getDateVente())
                .build();
    }
}
