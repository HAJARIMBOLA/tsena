package com.tsena.app.controller;

import com.tsena.app.dto.ProduitDTO;
import com.tsena.app.service.ProduitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
public class ProduitController {

    private final ProduitService produitService;

    public ProduitController(ProduitService produitService) {
        this.produitService = produitService;
    }

    @PostMapping("/admin/produits")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProduitDTO> creer(@Valid @RequestBody ProduitDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produitService.creer(dto));
    }

    @GetMapping("/admin/produits")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProduitDTO> lister() {
        return produitService.lister();
    }

    @GetMapping("/admin/produits/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProduitDTO detail(@PathVariable Long id) {
        return produitService.trouverParId(id);
    }

    @PutMapping("/admin/produits/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProduitDTO modifier(@PathVariable Long id, @Valid @RequestBody ProduitDTO dto) {
        return produitService.modifier(id, dto);
    }

    @DeleteMapping("/admin/produits/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        produitService.desactiver(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/produits")
    public List<ProduitDTO> listerActifs() {
        return produitService.listerActifs();
    }
}
