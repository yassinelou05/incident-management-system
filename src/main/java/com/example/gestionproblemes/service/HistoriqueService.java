package com.example.gestionproblemes.service;

import com.example.gestionproblemes.enums.StatutProbleme;
import com.example.gestionproblemes.enums.TypeAction;
import com.example.gestionproblemes.model.Historique;
import com.example.gestionproblemes.model.Probleme;
import com.example.gestionproblemes.model.Utilisateur;
import com.example.gestionproblemes.repository.HistoriqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Journalisation des actions importantes : tracabilite complete du cycle de vie. */
@Service
@RequiredArgsConstructor
public class HistoriqueService {

    private final HistoriqueRepository historiqueRepository;

    @Transactional
    public Historique enregistrer(Probleme probleme,
                                  TypeAction typeAction,
                                  StatutProbleme ancienStatut,
                                  StatutProbleme nouveauStatut,
                                  String description,
                                  Utilisateur auteur) {

        Historique historique = Historique.builder()
                .probleme(probleme)
                .typeAction(typeAction)
                .ancienStatut(ancienStatut)
                .nouveauStatut(nouveauStatut)
                .description(description)
                .auteur(auteur)
                .build();

        return historiqueRepository.save(historique);
    }

    @Transactional
    public Historique enregistrer(Probleme probleme, TypeAction typeAction,
                                  String description, Utilisateur auteur) {
        return enregistrer(probleme, typeAction, null, null, description, auteur);
    }

    @Transactional(readOnly = true)
    public List<Historique> historiqueDuProbleme(Long idProbleme) {
        return historiqueRepository.findByProblemeIdProblemeOrderByDateActionAsc(idProbleme);
    }
}
