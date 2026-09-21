package com.example.gestionproblemes.controller;

import com.example.gestionproblemes.enums.Role;
import com.example.gestionproblemes.enums.StatutCompte;
import com.example.gestionproblemes.model.Utilisateur;
import com.example.gestionproblemes.security.UtilisateurConnecte;
import com.example.gestionproblemes.service.AdministrateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Perimetre administrateur :
 *   - authentification et profil de l'utilisateur connecte ;
 *   - gestion des comptes utilisateurs, des roles et des etats de compte.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdministrateurController {

    private final AdministrateurService administrateurService;

    // ==================== Authentification ====================

    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> corps) {
        return ResponseEntity.ok(administrateurService.authentifier(corps));
    }

    @GetMapping("/auth/moi")
    public ResponseEntity<Utilisateur> profil() {
        return ResponseEntity.ok(UtilisateurConnecte.get());
    }

    @GetMapping("/auth/permissions")
    public ResponseEntity<Map<String, Object>> permissions() {
        Utilisateur utilisateur = UtilisateurConnecte.get();
        return ResponseEntity.ok(Map.of(
                "role", utilisateur.getRole(),
                "permissions", utilisateur.getPermissions()
        ));
    }

    @PatchMapping("/auth/mot-de-passe")
    public ResponseEntity<Map<String, String>> changerMotDePasse(@RequestBody Map<String, Object> corps) {
        administrateurService.changerMotDePasse(UtilisateurConnecte.get(), corps);
        return ResponseEntity.ok(Map.of("message", "Mot de passe modifie avec succes."));
    }

    /** L'API etant sans etat, la deconnexion consiste a supprimer le jeton cote client. */
    @PostMapping("/auth/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Deconnexion effectuee."));
    }

    // ==================== Gestion des comptes ====================

    @GetMapping("/utilisateurs")
    public ResponseEntity<List<Utilisateur>> rechercher(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) StatutCompte statut,
            @RequestParam(required = false) Long idDepartement,
            @RequestParam(required = false) String motCle) {
        return ResponseEntity.ok(administrateurService.rechercher(role, statut, idDepartement, motCle));
    }

    @GetMapping("/utilisateurs/{id}")
    public ResponseEntity<Utilisateur> consulter(@PathVariable Long id) {
        return ResponseEntity.ok(administrateurService.chargerUtilisateur(id));
    }

    @PostMapping("/utilisateurs")
    public ResponseEntity<Utilisateur> creer(@RequestBody Map<String, Object> corps) {
        return ResponseEntity.status(HttpStatus.CREATED).body(administrateurService.creer(corps));
    }

    @PutMapping("/utilisateurs/{id}")
    public ResponseEntity<Utilisateur> modifier(@PathVariable Long id,
                                                @RequestBody Map<String, Object> corps) {
        return ResponseEntity.ok(administrateurService.modifier(id, corps));
    }

    @PatchMapping("/utilisateurs/{id}/activer")
    public ResponseEntity<Utilisateur> activer(@PathVariable Long id) {
        return ResponseEntity.ok(administrateurService.changerStatutCompte(id, StatutCompte.ACTIF));
    }

    @PatchMapping("/utilisateurs/{id}/desactiver")
    public ResponseEntity<Utilisateur> desactiver(@PathVariable Long id) {
        return ResponseEntity.ok(administrateurService.changerStatutCompte(id, StatutCompte.INACTIF));
    }

    @PatchMapping("/utilisateurs/{id}/suspendre")
    public ResponseEntity<Utilisateur> suspendre(@PathVariable Long id) {
        return ResponseEntity.ok(administrateurService.changerStatutCompte(id, StatutCompte.SUSPENDU));
    }

    @PatchMapping("/utilisateurs/{id}/disponibilite")
    public ResponseEntity<Utilisateur> changerDisponibilite(@PathVariable Long id,
                                                            @RequestParam boolean disponible) {
        return ResponseEntity.ok(administrateurService.changerDisponibilite(id, disponible));
    }

    @PatchMapping("/utilisateurs/{id}/reinitialiser-mot-de-passe")
    public ResponseEntity<Map<String, String>> reinitialiser(@PathVariable Long id) {
        administrateurService.reinitialiserMotDePasse(id);
        return ResponseEntity.ok(Map.of("message", "Mot de passe reinitialise."));
    }
}
