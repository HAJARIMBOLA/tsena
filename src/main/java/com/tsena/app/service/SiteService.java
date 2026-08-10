package com.tsena.app.service;

import com.tsena.app.dto.SiteDTO;
import com.tsena.app.entity.Site;
import com.tsena.app.exception.ResourceNotFoundException;
import com.tsena.app.repository.SiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SiteService {

    private final SiteRepository siteRepository;

    public SiteService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    public SiteDTO creer(SiteDTO dto) {
        Site site = Site.builder()
                .nom(dto.getNom())
                .localisation(dto.getLocalisation())
                .actif(true)
                .build();

        return toDto(siteRepository.save(site));
    }

    @Transactional(readOnly = true)
    public List<SiteDTO> lister() {
        return siteRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SiteDTO trouverParId(Long id) {
        return toDto(getOuLeverException(id));
    }

    public SiteDTO modifier(Long id, SiteDTO dto) {
        Site site = getOuLeverException(id);
        site.setNom(dto.getNom());
        site.setLocalisation(dto.getLocalisation());
        return toDto(site);
    }

    public void desactiver(Long id) {
        Site site = getOuLeverException(id);
        site.setActif(false);
    }

    private Site getOuLeverException(Long id) {
        return siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site introuvable : " + id));
    }

    private SiteDTO toDto(Site site) {
        return SiteDTO.builder()
                .id(site.getId())
                .nom(site.getNom())
                .localisation(site.getLocalisation())
                .actif(site.getActif())
                .build();
    }
}
