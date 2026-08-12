package com.tsena.app.service;

import com.tsena.app.dto.ProduitDTO;
import com.tsena.app.entity.Produit;
import com.tsena.app.exception.ResourceNotFoundException;
import com.tsena.app.repository.ProduitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProduitService {

    private final ProduitRepository produitRepository;

    public ProduitService(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    public ProduitDTO creer(ProduitDTO dto) {
        Produit produit = Produit.builder()
                .nom(dto.getNom())
                .categorie(dto.getCategorie())
                .unite(dto.getUnite())
                .actif(true)
                .build();

        return toDto(produitRepository.save(produit));
    }

    @Transactional(readOnly = true)
    public List<ProduitDTO> lister() {
        return produitRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProduitDTO> listerActifs() {
        return produitRepository.findByActifTrue().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProduitDTO trouverParId(Long id) {
        return toDto(getOuLeverException(id));
    }

    public ProduitDTO modifier(Long id, ProduitDTO dto) {
        Produit produit = getOuLeverException(id);
        produit.setNom(dto.getNom());
        produit.setCategorie(dto.getCategorie());
        return toDto(produit);
    }

    public void desactiver(Long id) {
        Produit produit = getOuLeverException(id);
        produit.setActif(false);
    }

    private Produit getOuLeverException(Long id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable : " + id));
    }

    private ProduitDTO toDto(Produit produit) {
        return ProduitDTO.builder()
                .id(produit.getId())
                .nom(produit.getNom())
                .categorie(produit.getCategorie())
                .unite(produit.getUnite())
                .actif(produit.getActif())
                .build();
    }
}
