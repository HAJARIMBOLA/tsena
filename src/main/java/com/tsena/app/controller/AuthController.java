package com.tsena.app.controller;

import com.tsena.app.dto.InscriptionDTO;
import com.tsena.app.dto.LoginRequestDTO;
import com.tsena.app.dto.LoginResponseDTO;
import com.tsena.app.dto.SetupStatutDTO;
import com.tsena.app.entity.Site;
import com.tsena.app.entity.Utilisateur;
import com.tsena.app.security.JwtService;
import com.tsena.app.security.UtilisateurPrincipal;
import com.tsena.app.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UtilisateurService utilisateurService;

    public AuthController(AuthenticationManager authenticationManager,
                           JwtService jwtService,
                           UtilisateurService utilisateurService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.utilisateurService = utilisateurService;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getMotDePasse()));

        UtilisateurPrincipal principal = (UtilisateurPrincipal) authentication.getPrincipal();
        Utilisateur utilisateur = principal.getUtilisateur();

        String token = jwtService.genererToken(principal);

        return toLoginResponse(utilisateur, token);
    }

    @GetMapping("/setup-required")
    public SetupStatutDTO setupRequired() {
        return SetupStatutDTO.builder()
                .setupRequired(utilisateurService.setupRequis())
                .build();
    }

    @PostMapping("/register")
    public LoginResponseDTO register(@Valid @RequestBody InscriptionDTO dto) {
        Utilisateur utilisateur = utilisateurService.creerPremierAdmin(dto);

        String token = jwtService.genererToken(new UtilisateurPrincipal(utilisateur));

        return toLoginResponse(utilisateur, token);
    }

    private LoginResponseDTO toLoginResponse(Utilisateur utilisateur, String token) {
        return LoginResponseDTO.builder()
                .token(token)
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole())
                .siteIds(utilisateur.getSitesAutorises().stream()
                        .map(Site::getId)
                        .collect(Collectors.toSet()))
                .build();
    }
}
