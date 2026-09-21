package com.example.gestionproblemes.controller;

import com.example.gestionproblemes.enums.Priorite;
import com.example.gestionproblemes.enums.Role;
import com.example.gestionproblemes.enums.StatutProbleme;
import com.example.gestionproblemes.model.Categorie;
import com.example.gestionproblemes.model.Departement;
import com.example.gestionproblemes.model.Utilisateur;
import com.example.gestionproblemes.service.AdministrateurService;
import com.example.gestionproblemes.service.CategorieService;
import com.example.gestionproblemes.service.DepartementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Referentiels de l'application : categories, departements, techniciens, enumerations. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReferentielController {

    private final CategorieService categorieService;
    private final DepartementService departementService;
    private final AdministrateurService administrateurService;

    // -------------------------- Categories --------------------------

    @GetMapping("/categories")
    public ResponseEntity<List<Categorie>> listerCategories(
            @RequestParam(defaultValue = "false") boolean activesSeulement) {
        return ResponseEntity.ok(categorieService.lister(activesSeulement));
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<Categorie> consulterCategorie(@PathVariable Long id) {
        return ResponseEntity.ok(categorieService.charger(id));
    }

    @PostMapping("/categories")
    public ResponseEntity<Categorie> creerCategorie(@RequestBody Map<String, Object> corps) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categorieService.creer(corps));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<Categorie> modifierCategorie(@PathVariable Long id,
                                                       @RequestBody Map<String, Object> corps) {
        return ResponseEntity.ok(categorieService.modifier(id, corps));
    }

    @PatchMapping("/categories/{id}/activation")
    public ResponseEntity<Categorie> changerActivation(@PathVariable Long id,
                                                       @RequestParam boolean active) {
        return ResponseEntity.ok(categorieService.changerActivation(id, active));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> supprimerCategorie(@PathVariable Long id) {
        categorieService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------- Departements --------------------------

    @GetMapping("/departements")
    public ResponseEntity<List<Departement>> listerDepartements() {
        return ResponseEntity.ok(departementService.lister());
    }

    @GetMapping("/departements/{id}")
    public ResponseEntity<Departement> consulterDepartement(@PathVariable Long id) {
        return ResponseEntity.ok(departementService.charger(id));
    }

    @PostMapping("/departements")
    public ResponseEntity<Departement> creerDepartement(@RequestBody Map<String, Object> corps) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departementService.creer(corps));
    }

    @PutMapping("/departements/{id}")
    public ResponseEntity<Departement> modifierDepartement(@PathVariable Long id,
                                                           @RequestBody Map<String, Object> corps) {
        return ResponseEntity.ok(departementService.modifier(id, corps));
    }

    @DeleteMapping("/departements/{id}")
    public ResponseEntity<Void> supprimerDepartement(@PathVariable Long id) {
        departementService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------- Techniciens et enumerations --------------------------

    @GetMapping("/techniciens")
    public ResponseEntity<List<Utilisateur>> listerTechniciens() {
        return ResponseEntity.ok(administrateurService.techniciens());
    }

    @GetMapping("/referentiels/enumerations")
    public ResponseEntity<Map<String, Object>> enumerations() {
        return ResponseEntity.ok(Map.of(
                "statuts", Arrays.stream(StatutProbleme.values()).map(Enum::name).toList(),
                "priorites", Arrays.stream(Priorite.values()).map(Enum::name).toList(),
                "roles", Arrays.stream(Role.values()).map(Enum::name).toList()
        ));
    }
}
