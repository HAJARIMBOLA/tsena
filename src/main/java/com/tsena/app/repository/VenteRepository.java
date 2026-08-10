package com.tsena.app.repository;

import com.tsena.app.dto.SiteClassementDTO;
import com.tsena.app.dto.VenteAgregeeDTO;
import com.tsena.app.entity.Vente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface VenteRepository extends JpaRepository<Vente, Long> {

    List<Vente> findBySiteId(Long siteId);

    List<Vente> findBySiteIdAndDateVenteBetween(Long siteId, LocalDateTime debut, LocalDateTime fin);

    Page<Vente> findBySiteId(Long siteId, Pageable pageable);

    Page<Vente> findBySiteIdAndDateVenteBetween(Long siteId, LocalDateTime debut, LocalDateTime fin, Pageable pageable);

    Page<Vente> findByUtilisateurId(Long utilisateurId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(v.montantTotal), 0) FROM Vente v WHERE v.site.id = :siteId AND v.dateVente BETWEEN :debut AND :fin")
    BigDecimal sommeMontantParSite(@Param("siteId") Long siteId, @Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(v) FROM Vente v WHERE v.site.id = :siteId AND v.dateVente BETWEEN :debut AND :fin")
    Long compterParSite(@Param("siteId") Long siteId, @Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("SELECT COALESCE(SUM(v.quantite), 0) FROM Vente v WHERE v.site.id = :siteId AND v.dateVente BETWEEN :debut AND :fin")
    BigDecimal sommeQuantiteParSite(@Param("siteId") Long siteId, @Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("SELECT new com.tsena.app.dto.VenteAgregeeDTO(v.produit.nom, SUM(v.quantite), SUM(v.montantTotal)) "
            + "FROM Vente v WHERE v.site.id = :siteId AND v.dateVente BETWEEN :debut AND :fin "
            + "GROUP BY v.produit.nom ORDER BY SUM(v.montantTotal) DESC")
    List<VenteAgregeeDTO> topProduitsParSite(@Param("siteId") Long siteId, @Param("debut") LocalDateTime debut,
                                              @Param("fin") LocalDateTime fin, Pageable pageable);

    @Query(value = "SELECT date_trunc('day', v.date_vente) AS jour, SUM(v.montant_total) AS total "
            + "FROM vente v WHERE v.site_id = :siteId AND v.date_vente BETWEEN :debut AND :fin "
            + "GROUP BY jour ORDER BY jour", nativeQuery = true)
    List<Object[]> evolutionParSite(@Param("siteId") Long siteId, @Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("SELECT COALESCE(SUM(v.montantTotal), 0) FROM Vente v WHERE v.dateVente BETWEEN :debut AND :fin")
    BigDecimal sommeMontantGlobal(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(v) FROM Vente v WHERE v.dateVente BETWEEN :debut AND :fin")
    Long compterGlobal(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("SELECT COALESCE(SUM(v.quantite), 0) FROM Vente v WHERE v.dateVente BETWEEN :debut AND :fin")
    BigDecimal sommeQuantiteGlobal(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("SELECT new com.tsena.app.dto.VenteAgregeeDTO(v.produit.nom, SUM(v.quantite), SUM(v.montantTotal)) "
            + "FROM Vente v WHERE v.dateVente BETWEEN :debut AND :fin "
            + "GROUP BY v.produit.nom ORDER BY SUM(v.montantTotal) DESC")
    List<VenteAgregeeDTO> topProduitsGlobal(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin, Pageable pageable);

    @Query(value = "SELECT date_trunc('day', v.date_vente) AS jour, SUM(v.montant_total) AS total "
            + "FROM vente v WHERE v.date_vente BETWEEN :debut AND :fin "
            + "GROUP BY jour ORDER BY jour", nativeQuery = true)
    List<Object[]> evolutionGlobale(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("SELECT new com.tsena.app.dto.SiteClassementDTO(v.site.id, v.site.nom, SUM(v.montantTotal)) "
            + "FROM Vente v WHERE v.dateVente BETWEEN :debut AND :fin "
            + "GROUP BY v.site.id, v.site.nom ORDER BY SUM(v.montantTotal) DESC")
    List<SiteClassementDTO> classementSites(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
}
