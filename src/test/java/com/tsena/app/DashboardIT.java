package com.tsena.app;

import com.tsena.app.dto.DashboardSiteDTO;
import com.tsena.app.entity.Produit;
import com.tsena.app.entity.Role;
import com.tsena.app.entity.Site;
import com.tsena.app.entity.Unite;
import com.tsena.app.entity.Utilisateur;
import com.tsena.app.entity.Vente;
import com.tsena.app.repository.ProduitRepository;
import com.tsena.app.repository.SiteRepository;
import com.tsena.app.repository.UtilisateurRepository;
import com.tsena.app.repository.VenteRepository;
import com.tsena.app.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DashboardIT extends AbstractIntegrationTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private VenteRepository venteRepository;

    @Test
    void dashboardParSite_agregeCorrectementLesVentesDuJour() {
        Site site = siteRepository.save(Site.builder()
                .nom("Site Dashboard")
                .localisation("Antananarivo")
                .actif(true)
                .build());

        Produit riz = produitRepository.save(Produit.builder()
                .nom("Riz")
                .categorie("Cereales")
                .unite(Unite.SAC)
                .actif(true)
                .build());

        Produit sucre = produitRepository.save(Produit.builder()
                .nom("Sucre")
                .categorie("Epicerie")
                .unite(Unite.KG)
                .actif(true)
                .build());

        Utilisateur vendeur = utilisateurRepository.save(Utilisateur.builder()
                .nom("Vendeur")
                .email("vendeur-dashboard@tsena.mg")
                .motDePasse("peu-importe-non-utilise")
                .role(Role.EMPLOYE)
                .actif(true)
                .build());

        LocalDateTime maintenant = LocalDateTime.now();

        venteRepository.save(Vente.builder()
                .site(site).produit(riz).utilisateur(vendeur)
                .quantite(new BigDecimal("2")).montantTotal(new BigDecimal("2000"))
                .dateVente(maintenant)
                .build());
        venteRepository.save(Vente.builder()
                .site(site).produit(riz).utilisateur(vendeur)
                .quantite(new BigDecimal("1")).montantTotal(new BigDecimal("1000"))
                .dateVente(maintenant)
                .build());
        venteRepository.save(Vente.builder()
                .site(site).produit(sucre).utilisateur(vendeur)
                .quantite(new BigDecimal("3")).montantTotal(new BigDecimal("6000"))
                .dateVente(maintenant)
                .build());

        DashboardSiteDTO dashboard = dashboardService.dashboardParSite(site.getId(), "jour");

        assertThat(dashboard.getChiffreAffairesTotal()).isEqualByComparingTo(new BigDecimal("9000"));
        assertThat(dashboard.getNombreVentes()).isEqualTo(3L);
        assertThat(dashboard.getQuantiteVendueSacs()).isEqualByComparingTo(new BigDecimal("3"));
        assertThat(dashboard.getQuantiteVenduePoidsKg()).isEqualByComparingTo(new BigDecimal("3"));
        assertThat(dashboard.getTopProduits()).hasSize(2);
        assertThat(dashboard.getTopProduits().get(0).getProduitNom()).isEqualTo("Sucre");
        assertThat(dashboard.getTopProduits().get(0).getMontantTotal()).isEqualByComparingTo(new BigDecimal("6000"));
        assertThat(dashboard.getEvolution()).hasSize(1);
        assertThat(dashboard.getEvolution().get(0).getMontant()).isEqualByComparingTo(new BigDecimal("9000"));
    }
}
