package com.tsena.app.controller;

import com.tsena.app.dto.ChangementMotDePasseDTO;
import com.tsena.app.dto.UtilisateurDTO;
import com.tsena.app.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/moi")
public class CompteController {

    private final UtilisateurService utilisateurService;

    public CompteController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public UtilisateurDTO moi(Authentication authentication) {
        return utilisateurService.trouverParEmail(authentication.getName());
    }

    @PutMapping("/mot-de-passe")
    public ResponseEntity<Void> changerMotDePasse(@Valid @RequestBody ChangementMotDePasseDTO dto,
                                                    Authentication authentication) {
        utilisateurService.changerMotDePasse(authentication.getName(), dto);
        return ResponseEntity.noContent().build();
    }
}
