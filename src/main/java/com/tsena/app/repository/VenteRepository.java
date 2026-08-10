package com.tsena.app.repository;

import com.tsena.app.entity.Vente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VenteRepository extends JpaRepository<Vente, Long> {

    List<Vente> findBySiteId(Long siteId);

    List<Vente> findBySiteIdAndDateVenteBetween(Long siteId, LocalDateTime debut, LocalDateTime fin);
}
