package com.tsena.app;

import com.tsena.app.entity.Produit;
import com.tsena.app.entity.Role;
import com.tsena.app.entity.Site;
import com.tsena.app.entity.Unite;
import com.tsena.app.entity.Utilisateur;
import com.tsena.app.repository.ProduitRepository;
import com.tsena.app.repository.SiteRepository;
import com.tsena.app.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class TsenaApplicationIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void creeUnSiteUnProduitEtUnUtilisateur() {
        Site site = siteRepository.save(Site.builder()
                .nom("Depot Antananarivo")
                .localisation("Antananarivo")
                .actif(true)
                .build());

        Produit produit = produitRepository.save(Produit.builder()
                .nom("Riz blanc")
                .categorie("Cereales")
                .unite(Unite.SAC)
                .prixUnitaire(new BigDecimal("25000.00"))
                .actif(true)
                .build());

        Utilisateur utilisateur = utilisateurRepository.save(Utilisateur.builder()
                .nom("Rina")
                .email("rina@tsena.mg")
                .motDePasse("hash-bcrypt")
                .role(Role.EMPLOYE)
                .actif(true)
                .build());

        assertThat(site.getId()).isNotNull();
        assertThat(produit.getId()).isNotNull();
        assertThat(utilisateur.getId()).isNotNull();
        assertThat(utilisateurRepository.findByEmail("rina@tsena.mg")).isPresent();
    }
}
