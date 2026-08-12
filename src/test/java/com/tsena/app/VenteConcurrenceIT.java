package com.tsena.app;

import com.tsena.app.dto.VenteCreationDTO;
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
import com.tsena.app.security.UtilisateurPrincipal;
import com.tsena.app.service.VenteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class VenteConcurrenceIT extends AbstractIntegrationTest {

    @Autowired
    private VenteService venteService;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private StockSiteRepository stockSiteRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private VenteRepository venteRepository;

    @Test
    void deuxVentesSimultaneesNeSurvendentJamaisLeStock() throws Exception {
        Site site = siteRepository.save(Site.builder()
                .nom("Site Concurrence")
                .localisation("Antananarivo")
                .actif(true)
                .build());

        Produit produit = produitRepository.save(Produit.builder()
                .nom("Riz")
                .categorie("Cereales")
                .unite(Unite.SAC)
                .actif(true)
                .build());

        stockSiteRepository.save(StockSite.builder()
                .site(site)
                .produit(produit)
                .quantiteDisponible(new BigDecimal("5"))
                .prixUnitaire(new BigDecimal("1000"))
                .build());

        Set<Site> sitesAutorises = new HashSet<>(Set.of(site));
        Utilisateur employe = utilisateurRepository.save(Utilisateur.builder()
                .nom("Employe")
                .email("employe-concurrence@tsena.mg")
                .motDePasse("peu-importe-non-utilise")
                .role(Role.EMPLOYE)
                .actif(true)
                .sitesAutorises(sitesAutorises)
                .build());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new UtilisateurPrincipal(employe), null, List.of(new SimpleGrantedAuthority("ROLE_EMPLOYE")));

        VenteCreationDTO dto = new VenteCreationDTO();
        dto.setSiteId(site.getId());
        dto.setProduitId(produit.getId());
        dto.setQuantite(new BigDecimal("5"));

        CountDownLatch depart = new CountDownLatch(2);
        Callable<Boolean> tache = () -> {
            depart.countDown();
            depart.await(5, TimeUnit.SECONDS);
            try {
                venteService.creerVente(dto, authentication);
                return true;
            } catch (RuntimeException e) {
                return false;
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<Boolean> f1 = executor.submit(tache);
        Future<Boolean> f2 = executor.submit(tache);

        boolean r1 = f1.get(10, TimeUnit.SECONDS);
        boolean r2 = f2.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(r1 ^ r2).as("exactement une des deux ventes doit reussir").isTrue();

        StockSite stockFinal = stockSiteRepository.findBySiteIdAndProduitId(site.getId(), produit.getId())
                .orElseThrow();
        assertThat(stockFinal.getQuantiteDisponible()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(venteRepository.findBySiteId(site.getId())).hasSize(1);
    }
}
