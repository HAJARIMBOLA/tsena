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
import com.tsena.app.repository.VenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VenteIT extends AbstractIntegrationTest {

    private static final String MOT_DE_PASSE_CLAIR = "SecretTest123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private StockSiteRepository stockSiteRepository;

    @Autowired
    private VenteRepository venteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Site siteAutorise;
    private Site siteNonAutorise;
    private Produit produit;

    @BeforeEach
    void setUp() {
        siteAutorise = siteRepository.save(Site.builder()
                .nom("Site Autorise")
                .localisation("Antananarivo")
                .actif(true)
                .build());

        siteNonAutorise = siteRepository.save(Site.builder()
                .nom("Site Non Autorise")
                .localisation("Toamasina")
                .actif(true)
                .build());

        produit = produitRepository.save(Produit.builder()
                .nom("Riz")
                .categorie("Cereales")
                .unite(Unite.SAC)
                .prixUnitaire(new BigDecimal("1000"))
                .actif(true)
                .build());

        stockSiteRepository.save(StockSite.builder()
                .site(siteAutorise)
                .produit(produit)
                .quantiteDisponible(new BigDecimal("10"))
                .build());

        stockSiteRepository.save(StockSite.builder()
                .site(siteNonAutorise)
                .produit(produit)
                .quantiteDisponible(new BigDecimal("10"))
                .build());

        Set<Site> sitesAutorises = new HashSet<>(Set.of(siteAutorise));
        utilisateurRepository.save(Utilisateur.builder()
                .nom("Employe")
                .email("employe@tsena.mg")
                .motDePasse(passwordEncoder.encode(MOT_DE_PASSE_CLAIR))
                .role(Role.EMPLOYE)
                .actif(true)
                .sitesAutorises(sitesAutorises)
                .build());
    }

    @Test
    void venteReussie_decrementeStockEtCalculeLeMontant() throws Exception {
        String payload = """
                { "siteId": %d, "produitId": %d, "quantite": 3 }
                """.formatted(siteAutorise.getId(), produit.getId());

        mockMvc.perform(post("/ventes")
                        .with(httpBasic("employe@tsena.mg", MOT_DE_PASSE_CLAIR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantite").value(3))
                .andExpect(jsonPath("$.montantTotal").value(3000));

        StockSite stock = stockSiteRepository.findBySiteIdAndProduitId(siteAutorise.getId(), produit.getId())
                .orElseThrow();
        assertThat(stock.getQuantiteDisponible()).isEqualByComparingTo(new BigDecimal("7"));
        assertThat(venteRepository.findBySiteId(siteAutorise.getId())).hasSize(1);
    }

    @Test
    void venteAvecStockInsuffisant_rejeteeEtRienModifie() throws Exception {
        String payload = """
                { "siteId": %d, "produitId": %d, "quantite": 999 }
                """.formatted(siteAutorise.getId(), produit.getId());

        mockMvc.perform(post("/ventes")
                        .with(httpBasic("employe@tsena.mg", MOT_DE_PASSE_CLAIR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        StockSite stock = stockSiteRepository.findBySiteIdAndProduitId(siteAutorise.getId(), produit.getId())
                .orElseThrow();
        assertThat(stock.getQuantiteDisponible()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(venteRepository.findBySiteId(siteAutorise.getId())).isEmpty();
    }

    @Test
    void venteSurSiteNonAutorise_403() throws Exception {
        String payload = """
                { "siteId": %d, "produitId": %d, "quantite": 1 }
                """.formatted(siteNonAutorise.getId(), produit.getId());

        mockMvc.perform(post("/ventes")
                        .with(httpBasic("employe@tsena.mg", MOT_DE_PASSE_CLAIR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());

        StockSite stock = stockSiteRepository.findBySiteIdAndProduitId(siteNonAutorise.getId(), produit.getId())
                .orElseThrow();
        assertThat(stock.getQuantiteDisponible()).isEqualByComparingTo(new BigDecimal("10"));
    }
}
