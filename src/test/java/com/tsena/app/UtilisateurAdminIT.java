package com.tsena.app;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UtilisateurAdminIT extends AbstractIntegrationTest {

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
    private VenteRepository venteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void adminNePeutPasDesactiverSonPropreCompte() throws Exception {
        Utilisateur admin = utilisateurRepository.save(Utilisateur.builder()
                .nom("Admin Auto")
                .email("admin-auto-desactivation@tsena.mg")
                .motDePasse(passwordEncoder.encode(MOT_DE_PASSE_CLAIR))
                .role(Role.ADMIN)
                .actif(true)
                .build());

        mockMvc.perform(put("/api/admin/utilisateurs/{id}/desactiver", admin.getId())
                        .with(authentifieComme(admin)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Vous ne pouvez pas desactiver votre propre compte."));

        assertThat(utilisateurRepository.findById(admin.getId()).orElseThrow().getActif()).isTrue();
    }

    @Test
    void adminPeutDesactiverUnAutreCompte() throws Exception {
        Utilisateur admin = utilisateurRepository.save(Utilisateur.builder()
                .nom("Admin Principal")
                .email("admin-principal-desactivation@tsena.mg")
                .motDePasse(passwordEncoder.encode(MOT_DE_PASSE_CLAIR))
                .role(Role.ADMIN)
                .actif(true)
                .build());

        Utilisateur autre = utilisateurRepository.save(Utilisateur.builder()
                .nom("Autre Employe")
                .email("autre-employe-desactivation@tsena.mg")
                .motDePasse(passwordEncoder.encode(MOT_DE_PASSE_CLAIR))
                .role(Role.EMPLOYE)
                .actif(true)
                .build());

        mockMvc.perform(put("/api/admin/utilisateurs/{id}/desactiver", autre.getId())
                        .with(authentifieComme(admin)))
                .andExpect(status().isNoContent());

        assertThat(utilisateurRepository.findById(autre.getId()).orElseThrow().getActif()).isFalse();
    }

    @Test
    void adminPeutReactiverUnCompteDesactive() throws Exception {
        Utilisateur admin = utilisateurRepository.save(Utilisateur.builder()
                .nom("Admin Reactivation")
                .email("admin-reactivation@tsena.mg")
                .motDePasse(passwordEncoder.encode(MOT_DE_PASSE_CLAIR))
                .role(Role.ADMIN)
                .actif(true)
                .build());

        Utilisateur employe = utilisateurRepository.save(Utilisateur.builder()
                .nom("Employe Inactif")
                .email("employe-inactif@tsena.mg")
                .motDePasse(passwordEncoder.encode(MOT_DE_PASSE_CLAIR))
                .role(Role.EMPLOYE)
                .actif(false)
                .build());

        mockMvc.perform(put("/api/admin/utilisateurs/{id}/reactiver", employe.getId())
                        .with(authentifieComme(admin)))
                .andExpect(status().isNoContent());

        assertThat(utilisateurRepository.findById(employe.getId()).orElseThrow().getActif()).isTrue();
    }

    @Test
    void adminPeutSupprimerUnEmployeSansVentes() throws Exception {
        Utilisateur admin = utilisateurRepository.save(Utilisateur.builder()
                .nom("Admin Suppression")
                .email("admin-suppression@tsena.mg")
                .motDePasse(passwordEncoder.encode(MOT_DE_PASSE_CLAIR))
                .role(Role.ADMIN)
                .actif(true)
                .build());

        Utilisateur employe = utilisateurRepository.save(Utilisateur.builder()
                .nom("Employe Sans Ventes")
                .email("employe-sans-ventes@tsena.mg")
                .motDePasse(passwordEncoder.encode(MOT_DE_PASSE_CLAIR))
                .role(Role.EMPLOYE)
                .actif(true)
                .build());

        mockMvc.perform(delete("/api/admin/utilisateurs/{id}", employe.getId())
                        .with(authentifieComme(admin)))
                .andExpect(status().isNoContent());

        assertThat(utilisateurRepository.findById(employe.getId())).isEmpty();
    }

    @Test
    void adminNePeutPasSupprimerUnAdmin() throws Exception {
        Utilisateur admin = utilisateurRepository.save(Utilisateur.builder()
                .nom("Admin Cible")
                .email("admin-cible-suppression@tsena.mg")
                .motDePasse(passwordEncoder.encode(MOT_DE_PASSE_CLAIR))
                .role(Role.ADMIN)
                .actif(true)
                .build());

        Utilisateur autreAdmin = utilisateurRepository.save(Utilisateur.builder()
                .nom("Autre Admin")
                .email("autre-admin-suppression@tsena.mg")
                .motDePasse(passwordEncoder.encode(MOT_DE_PASSE_CLAIR))
                .role(Role.ADMIN)
                .actif(true)
                .build());

        mockMvc.perform(delete("/api/admin/utilisateurs/{id}", autreAdmin.getId())
                        .with(authentifieComme(admin)))
                .andExpect(status().isBadRequest());

        assertThat(utilisateurRepository.findById(autreAdmin.getId())).isPresent();
    }

    @Test
    void adminNePeutPasSupprimerUnEmployeAvecDesVentes() throws Exception {
        Utilisateur admin = utilisateurRepository.save(Utilisateur.builder()
                .nom("Admin Ventes")
                .email("admin-ventes-suppression@tsena.mg")
                .motDePasse(passwordEncoder.encode(MOT_DE_PASSE_CLAIR))
                .role(Role.ADMIN)
                .actif(true)
                .build());

        Utilisateur employe = utilisateurRepository.save(Utilisateur.builder()
                .nom("Employe Avec Ventes")
                .email("employe-avec-ventes@tsena.mg")
                .motDePasse(passwordEncoder.encode(MOT_DE_PASSE_CLAIR))
                .role(Role.EMPLOYE)
                .actif(true)
                .build());

        Site site = siteRepository.save(Site.builder()
                .nom("Site Suppression Test")
                .localisation("Test")
                .actif(true)
                .build());

        Produit produit = produitRepository.save(Produit.builder()
                .nom("Produit Suppression Test")
                .categorie("Test")
                .unite(Unite.SAC)
                .prixUnitaire(new BigDecimal("1000"))
                .actif(true)
                .build());

        venteRepository.save(Vente.builder()
                .site(site)
                .produit(produit)
                .utilisateur(employe)
                .quantite(new BigDecimal("1"))
                .montantTotal(new BigDecimal("1000"))
                .dateVente(LocalDateTime.now())
                .build());

        mockMvc.perform(delete("/api/admin/utilisateurs/{id}", employe.getId())
                        .with(authentifieComme(admin)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Ce compte a des ventes associees : desactivez-le plutot que de le supprimer."));

        assertThat(utilisateurRepository.findById(employe.getId())).isPresent();
    }
}
