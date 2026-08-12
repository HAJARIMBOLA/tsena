package com.tsena.app;

import com.tsena.app.entity.Produit;
import com.tsena.app.entity.Role;
import com.tsena.app.entity.Site;
import com.tsena.app.entity.StockSite;
import com.tsena.app.entity.Unite;
import com.tsena.app.entity.Utilisateur;
import com.tsena.app.repository.ProduitRepository;
import com.tsena.app.repository.SiteRepository;
import com.tsena.app.repository.StockSiteRepository;
import com.tsena.app.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardDiagIT extends AbstractIntegrationTest {

    private static final String MOT_DE_PASSE_CLAIR = "SecretTest123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private StockSiteRepository stockSiteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void venteFaiteMaintenant_apparaitDansLeDashboardDuJour() throws Exception {
        Utilisateur admin = utilisateurRepository.save(Utilisateur.builder()
                .nom("Admin Dashboard Diag")
                .email("admin-dashboard-diag@tsena.mg")
                .motDePasse(passwordEncoder.encode(MOT_DE_PASSE_CLAIR))
                .role(Role.ADMIN)
                .actif(true)
                .build());

        Site site = siteRepository.save(Site.builder()
                .nom("Site Dashboard Diag")
                .localisation("Test")
                .actif(true)
                .build());

        Produit produit = produitRepository.save(Produit.builder()
                .nom("Produit Dashboard Diag")
                .categorie("Cereales")
                .unite(Unite.SAC)
                .prixUnitaire(new BigDecimal("10000"))
                .actif(true)
                .build());

        stockSiteRepository.save(StockSite.builder()
                .site(site)
                .produit(produit)
                .quantiteDisponible(new BigDecimal("100"))
                .prixUnitaire(new BigDecimal("10000"))
                .build());

        String payloadVente = """
                { "siteId": %d, "produitId": %d, "quantite": 2 }
                """.formatted(site.getId(), produit.getId());

        mockMvc.perform(post("/api/ventes")
                        .with(authentifieComme(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadVente))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/dashboard/site/{siteId}", site.getId())
                        .param("periode", "jour")
                        .with(authentifieComme(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreVentes").value(1))
                .andExpect(jsonPath("$.chiffreAffairesTotal").value(20000));
    }
}
