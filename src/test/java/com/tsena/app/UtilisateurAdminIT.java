package com.tsena.app;

import com.tsena.app.entity.Role;
import com.tsena.app.entity.Utilisateur;
import com.tsena.app.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
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

        mockMvc.perform(put("/admin/utilisateurs/{id}/desactiver", admin.getId())
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

        mockMvc.perform(put("/admin/utilisateurs/{id}/desactiver", autre.getId())
                        .with(authentifieComme(admin)))
                .andExpect(status().isNoContent());

        assertThat(utilisateurRepository.findById(autre.getId()).orElseThrow().getActif()).isFalse();
    }
}
