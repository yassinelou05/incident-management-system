package com.example.gestionproblemes.controller;

import com.example.gestionproblemes.model.Historique;
import com.example.gestionproblemes.security.UtilisateurConnecte;
import com.example.gestionproblemes.service.HistoriqueService;
import com.example.gestionproblemes.service.ProblemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Consultation de l'historique et de la tracabilite d'un probleme. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HistoriqueController {

    private final HistoriqueService historiqueService;
    private final ProblemeService problemeService;

    @GetMapping("/problemes/{idProbleme}/historique")
    public ResponseEntity<List<Historique>> historiqueProbleme(@PathVariable Long idProbleme) {
        problemeService.verifierDroitDeLecture(problemeService.charger(idProbleme), UtilisateurConnecte.get());
        return ResponseEntity.ok(historiqueService.historiqueDuProbleme(idProbleme));
    }
}
