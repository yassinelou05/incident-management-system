package com.example.gestionproblemes.service;

import com.example.gestionproblemes.model.Departement;
import com.example.gestionproblemes.repository.DepartementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/** Referentiel des departements de l'organisation. */
@Service
@RequiredArgsConstructor
public class DepartementService {

    private final DepartementRepository departementRepository;

    @Transactional(readOnly = true)
    public List<Departement> lister() {
        return departementRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Departement charger(Long id) {
        return departementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Departement introuvable."));
    }

    @Transactional
    public Departement creer(Map<String, Object> corps) {
        String nom = RequeteUtils.texteObligatoire(corps, "nom", "nom");
        if (departementRepository.existsByNomIgnoreCase(nom)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un departement porte deja le nom " + nom + ".");
        }
        return departementRepository.save(Departement.builder()
                .nom(nom)
                .description(RequeteUtils.texte(corps, "description"))
                .localisation(RequeteUtils.texte(corps, "localisation"))
                .build());
    }

    @Transactional
    public Departement modifier(Long id, Map<String, Object> corps) {
        Departement departement = charger(id);
        departement.setNom(RequeteUtils.texteObligatoire(corps, "nom", "nom"));
        departement.setDescription(RequeteUtils.texte(corps, "description"));
        departement.setLocalisation(RequeteUtils.texte(corps, "localisation"));
        return departementRepository.save(departement);
    }

    @Transactional
    public void supprimer(Long id) {
        Departement departement = charger(id);
        if (departement.getNombreUtilisateurs() > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ce departement ne peut pas etre supprime : des utilisateurs y sont rattaches.");
        }
        departementRepository.delete(departement);
    }
}
