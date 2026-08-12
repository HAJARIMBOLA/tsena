package com.tsena.app.controller;

import com.tsena.app.dto.ReapprovisionnementDTO;
import com.tsena.app.dto.StockSiteDTO;
import com.tsena.app.service.StockSiteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StockController {

    private final StockSiteService stockSiteService;

    public StockController(StockSiteService stockSiteService) {
        this.stockSiteService = stockSiteService;
    }

    @PostMapping("/admin/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockSiteDTO> affecter(@Valid @RequestBody StockSiteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockSiteService.affecter(dto));
    }

    @PutMapping("/stock/{siteId}/{produitId}")
    @PreAuthorize("hasRole('ADMIN') or @siteAccessService.aAcces(#siteId, authentication)")
    public StockSiteDTO reapprovisionner(@PathVariable Long siteId,
                                          @PathVariable Long produitId,
                                          @Valid @RequestBody ReapprovisionnementDTO dto) {
        return stockSiteService.reapprovisionner(siteId, produitId, dto.getQuantite());
    }

    @GetMapping("/stock/site/{siteId}")
    @PreAuthorize("hasRole('ADMIN') or @siteAccessService.aAcces(#siteId, authentication)")
    public List<StockSiteDTO> listerParSite(@PathVariable Long siteId) {
        return stockSiteService.listerParSite(siteId);
    }

    @GetMapping("/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public List<StockSiteDTO> listerTout() {
        return stockSiteService.listerTout();
    }
}
