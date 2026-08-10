package com.tsena.app.service;

import com.tsena.app.dto.CreationUtilisateurDTO;
import com.tsena.app.dto.UtilisateurDTO;
import com.tsena.app.entity.Role;
import com.tsena.app.entity.Site;
import com.tsena.app.entity.Utilisateur;
import com.tsena.app.exception.ConflitException;
import com.tsena.app.exception.OperationNonAutoriseeException;
import com.tsena.app.exception.ResourceNotFoundException;
import com.tsena.app.repository.SiteRepository;
import com.tsena.app.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final SiteRepository siteRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                               SiteRepository siteRepository,
                               PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.siteRepository = siteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UtilisateurDTO creer(CreationUtilisateurDTO dto) {
        if (utilisateurRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ConflitException("Un utilisateur existe deja avec cet email : " + dto.getEmail());
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(dto.getNom())
                .email(dto.getEmail())
                .motDePasse(passwordEncoder.encode(dto.getMotDePasse()))
                .role(dto.getRole())
                .actif(true)
                .sitesAutorises(resoudreSitesPourRole(dto.getRole(), dto.getSiteIds()))
                .build();

        return toDto(utilisateurRepository.save(utilisateur));
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDTO> lister() {
        return utilisateurRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UtilisateurDTO trouverParId(Long id) {
        return toDto(getOuLeverException(id));
    }

    public UtilisateurDTO modifierSites(Long id, Set<Long> siteIds) {
        Utilisateur utilisateur = getOuLeverException(id);

        if (utilisateur.getRole() == Role.ADMIN) {
            throw new OperationNonAutoriseeException(
                    "Un administrateur a un acces global implicite : la gestion des sites autorises ne s'applique pas.");
        }

        utilisateur.setSitesAutorises(resoudreSites(siteIds));
        return toDto(utilisateur);
    }

    public void desactiver(Long id) {
        Utilisateur utilisateur = getOuLeverException(id);
        utilisateur.setActif(false);
    }

    private Set<Site> resoudreSitesPourRole(Role role, Set<Long> siteIds) {
        if (role == Role.ADMIN) {
            return new HashSet<>();
        }
        return resoudreSites(siteIds);
    }

    private Set<Site> resoudreSites(Set<Long> siteIds) {
        if (siteIds == null || siteIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Site> sites = siteRepository.findAllById(siteIds);
        if (sites.size() != siteIds.size()) {
            throw new ResourceNotFoundException("Un ou plusieurs sites sont introuvables : " + siteIds);
        }
        return new HashSet<>(sites);
    }

    private Utilisateur getOuLeverException(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
    }

    private UtilisateurDTO toDto(Utilisateur utilisateur) {
        return UtilisateurDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole())
                .actif(utilisateur.getActif())
                .siteIds(utilisateur.getSitesAutorises().stream()
                        .map(Site::getId)
                        .collect(Collectors.toSet()))
                .build();
    }
}
