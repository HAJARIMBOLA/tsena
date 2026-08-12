package com.tsena.app;

import com.jayway.jsonpath.JsonPath;
import com.tsena.app.entity.Produit;
import com.tsena.app.entity.Role;
import com.tsena.app.entity.Unite;
import com.tsena.app.entity.Utilisateur;
import com.tsena.app.repository.ProduitRepository;
import com.tsena.app.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Bout en bout : un ADMIN cree un site, l'ajoute a son stock, puis vend
 * dessus, sans jamais avoir besoin de se reconnecter entre les etapes.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AdminSiteVenteFlowIT extends AbstractIntegrationTest {

    private static final String MOT_DE_PASSE_CLAIR = "SecretTest123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void adminCreeSiteAjouteStockEtVendDessus_sansReconnexion() throws Exception {
        Utilisateur admin = utilisateurRepository.save(Utilisateur.builder()
                .nom("Admin Flux")
                .email("admin-flux-site-vente@tsena.mg")
                .motDePasse(passwordEncoder.encode(MOT_DE_PASSE_CLAIR))
                .role(Role.ADMIN)
                .actif(true)
                .build());

        Produit produit = produitRepository.save(Produit.builder()
                .nom("Produit Flux")
                .categorie("Cereales")
                .unite(Unite.SAC)
                .actif(true)
                .build());

        String payloadSite = """
                { "nom": "Site Nouvellement Cree", "localisation": "Fianarantsoa" }
                """;

        String reponseSite = mockMvc.perform(post("/api/admin/sites")
                        .with(authentifieComme(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadSite))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long siteId = ((Number) JsonPath.read(reponseSite, "$.id")).longValue();

        mockMvc.perform(get("/api/mes-sites").with(authentifieComme(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + siteId + ")]").exists());

        String payloadStock = """
                { "siteId": %d, "produitId": %d, "quantiteDisponible": 50, "prixUnitaire": 15000 }
                """.formatted(siteId, produit.getId());

        mockMvc.perform(post("/api/admin/stock")
                        .with(authentifieComme(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadStock))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantiteDisponible").value(50));

        String payloadVente = """
                { "siteId": %d, "produitId": %d, "quantite": 3 }
                """.formatted(siteId, produit.getId());

        mockMvc.perform(post("/api/ventes")
                        .with(authentifieComme(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadVente))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.montantTotal").value(45000));

        mockMvc.perform(get("/api/stock/site/{siteId}", siteId).with(authentifieComme(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantiteDisponible").value(47));
    }
}
