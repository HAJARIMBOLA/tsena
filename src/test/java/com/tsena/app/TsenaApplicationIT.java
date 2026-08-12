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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TsenaApplicationIT extends AbstractIntegrationTest {

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
