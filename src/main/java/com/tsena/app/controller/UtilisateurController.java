package com.tsena.app.controller;

import com.tsena.app.dto.CreationUtilisateurDTO;
import com.tsena.app.dto.UtilisateurDTO;
import com.tsena.app.service.UtilisateurService;
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
import java.util.Set;

@RestController
@RequestMapping("/admin/utilisateurs")
@PreAuthorize("hasRole('ADMIN')")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @PostMapping
    public ResponseEntity<UtilisateurDTO> creer(@Valid @RequestBody CreationUtilisateurDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.creer(dto));
    }

    @GetMapping
    public List<UtilisateurDTO> lister() {
        return utilisateurService.lister();
    }

    @GetMapping("/{id}")
    public UtilisateurDTO detail(@PathVariable Long id) {
        return utilisateurService.trouverParId(id);
    }

    @PutMapping("/{id}/sites")
    public UtilisateurDTO modifierSites(@PathVariable Long id, @RequestBody Set<Long> siteIds) {
        return utilisateurService.modifierSites(id, siteIds);
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        utilisateurService.desactiver(id);
        return ResponseEntity.noContent().build();
    }
}
