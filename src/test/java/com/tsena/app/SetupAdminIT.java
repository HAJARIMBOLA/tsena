package com.tsena.app;

import com.jayway.jsonpath.JsonPath;
import com.tsena.app.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Utilise son propre conteneur Postgres (au lieu de celui, partage entre toutes
 * les classes IT, d'AbstractIntegrationTest) pour garantir une base reellement
 * vide au demarrage, condition necessaire aux scenarios de premier setup.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class SetupAdminIT {

    private static final String JWT_SECRET_TEST =
            "test-secret-key-for-integration-tests-only-do-not-use-in-prod-32bytes-min";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void proprietesDynamiques(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("jwt.secret", () -> JWT_SECRET_TEST);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @BeforeEach
    void nettoyer() {
        utilisateurRepository.deleteAll();
    }

    @Test
    void setupRequired_retourneTrue_surBaseVide() throws Exception {
        assertThat(utilisateurRepository.count()).isZero();

        mockMvc.perform(get("/api/auth/setup-required"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setupRequired").value(true));
    }

    @Test
    void register_surBaseVide_creeUnAdminEtRetourneUnToken() throws Exception {
        String payload = """
                { "nom": "Premier Admin", "email": "premier-admin@tsena.mg", "motDePasse": "SecretTest123!" }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.email").value("premier-admin@tsena.mg"));

        assertThat(utilisateurRepository.findByEmail("premier-admin@tsena.mg"))
                .hasValueSatisfying(u -> assertThat(u.getRole().name()).isEqualTo("ADMIN"));
    }

    @Test
    void deuxiemeRegister_apresLePremier_estRefuse403() throws Exception {
        String premierPayload = """
                { "nom": "Premier Admin", "email": "admin-unique@tsena.mg", "motDePasse": "SecretTest123!" }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(premierPayload))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/setup-required"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setupRequired").value(false));

        String secondPayload = """
                { "nom": "Second Admin", "email": "autre-admin@tsena.mg", "motDePasse": "SecretTest123!" }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondPayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(
                        "Un compte administrateur existe deja. Contactez votre administrateur pour obtenir un acces."));

        assertThat(utilisateurRepository.findByEmail("autre-admin@tsena.mg")).isEmpty();
    }

    @Test
    void register_neCreeJamaisUnEmploye_leRoleEstToujoursForceCoteServeur() throws Exception {
        String payloadAvecRoleFournit = """
                { "nom": "Tentative", "email": "tentative-employe@tsena.mg", "motDePasse": "SecretTest123!", "role": "EMPLOYE" }
                """;

        String reponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadAvecRoleFournit))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String role = JsonPath.read(reponse, "$.role");
        assertThat(role).isEqualTo("ADMIN");

        assertThat(utilisateurRepository.findByEmail("tentative-employe@tsena.mg"))
                .hasValueSatisfying(u -> assertThat(u.getRole().name()).isEqualTo("ADMIN"));
    }
}
