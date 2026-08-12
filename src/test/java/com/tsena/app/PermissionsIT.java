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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PermissionsIT extends AbstractIntegrationTest {

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
    private PasswordEncoder passwordEncoder;

    private Site siteAutorise;
    private Site siteNonAutorise;
    private Produit produit;
    private Utilisateur admin;
    private Utilisateur employe;

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
                .actif(true)
                .build());

        stockSiteRepository.save(StockSite.builder()
                .site(siteAutorise)
                .produit(produit)
                .quantiteDisponible(new BigDecimal("100"))
                .prixUnitaire(new BigDecimal("20000"))
                .build());

        stockSiteRepository.save(StockSite.builder()
                .site(siteNonAutorise)
                .produit(produit)
                .quantiteDisponible(new BigDecimal("50"))
                .prixUnitaire(new BigDecimal("20000"))
                .build());

        admin = utilisateurRepository.save(Utilisateur.builder()
                .nom("Admin")
                .email("admin@tsena.mg")
                .motDePasse(passwordEncoder.encode(MOT_DE_PASSE_CLAIR))
                .role(Role.ADMIN)
                .actif(true)
                .build());

        Set<Site> sitesAutorises = new HashSet<>(Set.of(siteAutorise));
        employe = utilisateurRepository.save(Utilisateur.builder()
                .nom("Employe")
                .email("employe@tsena.mg")
                .motDePasse(passwordEncoder.encode(MOT_DE_PASSE_CLAIR))
                .role(Role.EMPLOYE)
                .actif(true)
                .sitesAutorises(sitesAutorises)
                .build());
    }

    @Test
    void employeRefuseSurSiteNonAutorise() throws Exception {
        mockMvc.perform(get("/api/stock/site/{siteId}", siteNonAutorise.getId())
                        .with(authentifieComme(employe)))
                .andExpect(status().isForbidden());
    }

    @Test
    void employeAutoriseSurSonPropreSite() throws Exception {
        mockMvc.perform(get("/api/stock/site/{siteId}", siteAutorise.getId())
                        .with(authentifieComme(employe)))
                .andExpect(status().isOk());
    }

    @Test
    void adminAutoriseSurNimporteQuelSite() throws Exception {
        mockMvc.perform(get("/api/stock/site/{siteId}", siteNonAutorise.getId())
                        .with(authentifieComme(admin)))
                .andExpect(status().isOk());
    }

    @Test
    void employeAutoriseAffecterNouveauProduitSurSonPropreSite() throws Exception {
        Produit sucre = produitRepository.save(Produit.builder()
                .nom("Sucre")
                .categorie("Epicerie")
                .unite(Unite.KG)
                .actif(true)
                .build());

        String payload = """
                { "siteId": %d, "produitId": %d, "quantiteDisponible": 30, "prixUnitaire": 3000 }
                """.formatted(siteAutorise.getId(), sucre.getId());

        mockMvc.perform(post("/api/stock")
                        .with(authentifieComme(employe))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());
    }

    @Test
    void employeRefuseAffecterSurSiteNonAutorise() throws Exception {
        Produit sucre = produitRepository.save(Produit.builder()
                .nom("Sucre")
                .categorie("Epicerie")
                .unite(Unite.KG)
                .actif(true)
                .build());

        String payload = """
                { "siteId": %d, "produitId": %d, "quantiteDisponible": 30, "prixUnitaire": 3000 }
                """.formatted(siteNonAutorise.getId(), sucre.getId());

        mockMvc.perform(post("/api/stock")
                        .with(authentifieComme(employe))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void employeRefuseModifierPrixMemeSurSonPropreSite() throws Exception {
        String payload = """
                { "prixUnitaire": 25000 }
                """;

        mockMvc.perform(put("/api/admin/stock/{siteId}/{produitId}/prix", siteAutorise.getId(), produit.getId())
                        .with(authentifieComme(employe))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void employeRefuseSurCreationUtilisateur() throws Exception {
        String payload = """
                {
                  "nom": "Nouveau",
                  "email": "nouveau@tsena.mg",
                  "motDePasse": "AutreSecret123!",
                  "role": "EMPLOYE",
                  "siteIds": []
                }
                """;

        mockMvc.perform(post("/api/admin/utilisateurs")
                        .with(authentifieComme(employe))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }
}
