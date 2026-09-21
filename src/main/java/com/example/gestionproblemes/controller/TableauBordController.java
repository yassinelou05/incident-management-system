package com.example.gestionproblemes.controller;

import com.example.gestionproblemes.security.UtilisateurConnecte;
import com.example.gestionproblemes.service.TableauBordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Indicateurs et statistiques. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TableauBordController {

    private final TableauBordService tableauBordService;

    /** Vue adaptee au role de l'utilisateur connecte. */
    @GetMapping("/tableau-bord")
    public ResponseEntity<Map<String, Object>> tableauBord() {
        return ResponseEntity.ok(tableauBordService.statistiquesPersonnelles(UtilisateurConnecte.get()));
    }

    /** Vue globale reservee au responsable support et a l'administrateur. */
    @GetMapping("/tableau-bord/statistiques")
    public ResponseEntity<Map<String, Object>> statistiques() {
        return ResponseEntity.ok(tableauBordService.statistiquesGlobales());
    }

    @GetMapping("/sante")
    public ResponseEntity<Map<String, String>> sante() {
        return ResponseEntity.ok(Map.of("statut", "UP", "application", "gestion-problemes"));
    }
}
