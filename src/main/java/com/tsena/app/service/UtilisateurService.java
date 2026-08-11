package com.tsena.app.service;

import com.tsena.app.dto.ChangementMotDePasseDTO;
import com.tsena.app.dto.CreationUtilisateurDTO;
import com.tsena.app.dto.InscriptionDTO;
import com.tsena.app.dto.UtilisateurDTO;
import com.tsena.app.entity.Role;
import com.tsena.app.entity.Site;
import com.tsena.app.entity.Utilisateur;
import com.tsena.app.exception.ConflitException;
import com.tsena.app.exception.MotDePasseIncorrectException;
import com.tsena.app.exception.OperationNonAutoriseeException;
import com.tsena.app.exception.ResourceNotFoundException;
import com.tsena.app.exception.SetupDejaEffectueException;
import com.tsena.app.repository.SiteRepository;
import com.tsena.app.repository.UtilisateurRepository;
import com.tsena.app.repository.VenteRepository;
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
    private final VenteRepository venteRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                               SiteRepository siteRepository,
                               VenteRepository venteRepository,
                               PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.siteRepository = siteRepository;
        this.venteRepository = venteRepository;
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

    @Transactional(readOnly = true)
    public UtilisateurDTO trouverParEmail(String email) {
        return toDto(utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + email)));
    }

    public void changerMotDePasse(String email, ChangementMotDePasseDTO dto) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + email));

        if (!passwordEncoder.matches(dto.getMotDePasseActuel(), utilisateur.getMotDePasse())) {
            throw new MotDePasseIncorrectException("Mot de passe actuel incorrect.");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(dto.getNouveauMotDePasse()));
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

    public void desactiver(Long id, String emailAuthentifie) {
        Utilisateur utilisateur = getOuLeverException(id);
        if (utilisateur.getEmail().equalsIgnoreCase(emailAuthentifie)) {
            throw new OperationNonAutoriseeException("Vous ne pouvez pas desactiver votre propre compte.");
        }
        utilisateur.setActif(false);
    }

    public void reactiver(Long id) {
        Utilisateur utilisateur = getOuLeverException(id);
        utilisateur.setActif(true);
    }

    public void supprimer(Long id) {
        Utilisateur utilisateur = getOuLeverException(id);

        if (utilisateur.getRole() != Role.EMPLOYE) {
            throw new OperationNonAutoriseeException(
                    "Seuls les comptes employe peuvent etre supprimes definitivement. Desactivez plutot ce compte administrateur.");
        }

        if (venteRepository.existsByUtilisateurId(id)) {
            throw new ConflitException(
                    "Ce compte a des ventes associees : desactivez-le plutot que de le supprimer.");
        }

        utilisateurRepository.delete(utilisateur);
    }

    @Transactional(readOnly = true)
    public boolean setupRequis() {
        return utilisateurRepository.count() == 0;
    }

    public Utilisateur creerPremierAdmin(InscriptionDTO dto) {
        if (utilisateurRepository.count() > 0) {
            throw new SetupDejaEffectueException(
                    "Un compte administrateur existe deja. Contactez votre administrateur pour obtenir un acces.");
        }

        if (utilisateurRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ConflitException("Un utilisateur existe deja avec cet email : " + dto.getEmail());
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(dto.getNom())
                .email(dto.getEmail())
                .motDePasse(passwordEncoder.encode(dto.getMotDePasse()))
                .role(Role.ADMIN)
                .actif(true)
                .sitesAutorises(new HashSet<>())
                .build();

        return utilisateurRepository.save(utilisateur);
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
