package com.tsena.app.controller;

import com.tsena.app.dto.SiteDTO;
import com.tsena.app.security.UtilisateurPrincipal;
import com.tsena.app.service.SiteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
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
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @PostMapping("/admin/sites")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SiteDTO> creer(@Valid @RequestBody SiteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(siteService.creer(dto));
    }

    @GetMapping("/admin/sites")
    @PreAuthorize("hasRole('ADMIN')")
    public List<SiteDTO> lister() {
        return siteService.lister();
    }

    @PutMapping("/admin/sites/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SiteDTO modifier(@PathVariable Long id, @Valid @RequestBody SiteDTO dto) {
        return siteService.modifier(id, dto);
    }

    @DeleteMapping("/admin/sites/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        siteService.desactiver(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mes-sites")
    public List<SiteDTO> mesSites(Authentication authentication) {
        boolean estAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (estAdmin) {
            return siteService.lister();
        }

        if (authentication.getPrincipal() instanceof UtilisateurPrincipal principal) {
            return principal.getSitesAutorises().stream()
                    .map(site -> SiteDTO.builder()
                            .id(site.getId())
                            .nom(site.getNom())
                            .localisation(site.getLocalisation())
                            .actif(site.getActif())
                            .build())
                    .toList();
        }

        return List.of();
    }
}
