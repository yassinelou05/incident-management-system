package com.example.gestionproblemes.controller;

import com.example.gestionproblemes.model.Notification;
import com.example.gestionproblemes.security.UtilisateurConnecte;
import com.example.gestionproblemes.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Notifications de l'utilisateur connecte. */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> mesNotifications() {
        return ResponseEntity.ok(
                notificationService.mesNotifications(UtilisateurConnecte.get().getIdUtilisateur()));
    }

    @GetMapping("/non-lues/compte")
    public ResponseEntity<Map<String, Long>> compterNonLues() {
        return ResponseEntity.ok(Map.of("nonLues",
                notificationService.compterNonLues(UtilisateurConnecte.get().getIdUtilisateur())));
    }

    @PatchMapping("/{id}/lue")
    public ResponseEntity<Notification> marquerCommeLue(@PathVariable Long id) {
        return ResponseEntity.ok(
                notificationService.marquerCommeLue(id, UtilisateurConnecte.get().getIdUtilisateur()));
    }

    @PatchMapping("/toutes-lues")
    public ResponseEntity<Map<String, Integer>> marquerToutesCommeLues() {
        int nombre = notificationService.marquerToutesCommeLues(UtilisateurConnecte.get().getIdUtilisateur());
        return ResponseEntity.ok(Map.of("misesAJour", nombre));
    }
}
