package com.tsena.app;

import com.jayway.jsonpath.JsonPath;
import com.tsena.app.entity.Role;
import com.tsena.app.entity.Utilisateur;
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

@SpringBootTest
@AutoConfigureMockMvc
class AuthIT extends AbstractIntegrationTest {

    private static final String MOT_DE_PASSE_CLAIR = "SecretTest123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void loginReussi_retourneUnTokenEtLesInfosUtilisateur() throws Exception {
        utilisateurRepository.save(Utilisateur.builder()
                .nom("Admin")
                .email("admin-login@tsena.mg")
                .motDePasse(passwordEncoder.encode(MOT_DE_PASSE_CLAIR))
                .role(Role.ADMIN)
                .actif(true)
                .build());

        String payload = """
                { "email": "admin-login@tsena.mg", "motDePasse": "%s" }
                """.formatted(MOT_DE_PASSE_CLAIR);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.email").value("admin-login@tsena.mg"));
    }

    @Test
    void loginAvecMauvaisMotDePasse_401() throws Exception {
        utilisateurRepository.save(Utilisateur.builder()
                .nom("Employe")
                .email("employe-login@tsena.mg")
                .motDePasse(passwordEncoder.encode(MOT_DE_PASSE_CLAIR))
                .role(Role.EMPLOYE)
                .actif(true)
                .build());

        String payload = """
                { "email": "employe-login@tsena.mg", "motDePasse": "MauvaisMotDePasse" }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requeteSansToken_401() throws Exception {
        mockMvc.perform(get("/api/admin/sites"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenObtenuALaConnexion_permetDaccederAUneRessourceProtegee() throws Exception {
        utilisateurRepository.save(Utilisateur.builder()
                .nom("Admin2")
                .email("admin-token@tsena.mg")
                .motDePasse(passwordEncoder.encode(MOT_DE_PASSE_CLAIR))
                .role(Role.ADMIN)
                .actif(true)
                .build());

        String payload = """
                { "email": "admin-token@tsena.mg", "motDePasse": "%s" }
                """.formatted(MOT_DE_PASSE_CLAIR);

        String reponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(reponse, "$.token");

        mockMvc.perform(get("/api/admin/sites").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
