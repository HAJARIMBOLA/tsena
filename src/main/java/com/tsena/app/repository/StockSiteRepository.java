package com.tsena.app.repository;

import com.tsena.app.entity.StockSite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockSiteRepository extends JpaRepository<StockSite, Long> {

    Optional<StockSite> findBySiteIdAndProduitId(Long siteId, Long produitId);
}
