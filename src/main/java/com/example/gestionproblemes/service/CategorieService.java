package com.example.gestionproblemes.service;

import com.example.gestionproblemes.model.Categorie;
import com.example.gestionproblemes.repository.CategorieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/** Referentiel des categories de problemes. */
@Service
@RequiredArgsConstructor
public class CategorieService {

    private final CategorieRepository categorieRepository;

    @Transactional(readOnly = true)
    public List<Categorie> lister(boolean activesSeulement) {
        return activesSeulement ? categorieRepository.findByActiveTrue() : categorieRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Categorie charger(Long id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categorie introuvable."));
    }

    @Transactional
    public Categorie creer(Map<String, Object> corps) {
        String nom = RequeteUtils.texteObligatoire(corps, "nom", "nom");
        if (categorieRepository.existsByNomIgnoreCase(nom)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Une categorie porte deja le nom " + nom + ".");
        }
        return categorieRepository.save(Categorie.builder()
                .nom(nom)
                .description(RequeteUtils.texte(corps, "description"))
                .delaiCibleHeures(RequeteUtils.entier(corps, "delaiCibleHeures", 48))
                .active(RequeteUtils.booleen(corps, "active", Boolean.TRUE))
                .build());
    }

    @Transactional
    public Categorie modifier(Long id, Map<String, Object> corps) {
        Categorie categorie = charger(id);
        categorie.setNom(RequeteUtils.texteObligatoire(corps, "nom", "nom"));
        categorie.setDescription(RequeteUtils.texte(corps, "description"));
        categorie.setDelaiCibleHeures(
                RequeteUtils.entier(corps, "delaiCibleHeures", categorie.getDelaiCibleHeures()));
        categorie.setActive(RequeteUtils.booleen(corps, "active", categorie.getActive()));
        return categorieRepository.save(categorie);
    }

    @Transactional
    public Categorie changerActivation(Long id, boolean active) {
        Categorie categorie = charger(id);
        if (active) {
            categorie.activer();
        } else {
            categorie.desactiver();
        }
        return categorieRepository.save(categorie);
    }

    @Transactional
    public void supprimer(Long id) {
        Categorie categorie = charger(id);
        if (categorie.getProblemes() != null && !categorie.getProblemes().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cette categorie est utilisee par des problemes : desactivez-la plutot que de la supprimer.");
        }
        categorieRepository.delete(categorie);
    }
}
