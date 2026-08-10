package com.tsena.app.controller;

import com.tsena.app.dto.VenteCreationDTO;
import com.tsena.app.dto.VenteDTO;
import com.tsena.app.service.VenteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/ventes")
public class VenteController {

    private final VenteService venteService;

    public VenteController(VenteService venteService) {
        this.venteService = venteService;
    }

    @PostMapping
    public ResponseEntity<VenteDTO> creer(@Valid @RequestBody VenteCreationDTO dto, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(venteService.creerVente(dto, authentication));
    }

    @GetMapping("/site/{siteId}")
    public Page<VenteDTO> historiqueParSite(@PathVariable Long siteId,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
                                             Pageable pageable,
                                             Authentication authentication) {
        return venteService.historiqueParSite(siteId, debut, fin, pageable, authentication);
    }

    @GetMapping("/mes-ventes")
    public Page<VenteDTO> mesVentes(Pageable pageable, Authentication authentication) {
        return venteService.mesVentes(authentication, pageable);
    }

    @GetMapping("/{id}")
    public VenteDTO detail(@PathVariable Long id, Authentication authentication) {
        return venteService.trouverParId(id, authentication);
    }
}
