package com.example.gestionproblemes.controller;

import com.example.gestionproblemes.enums.Priorite;
import com.example.gestionproblemes.enums.StatutProbleme;
import com.example.gestionproblemes.model.Affectation;
import com.example.gestionproblemes.model.Probleme;
import com.example.gestionproblemes.model.Technicien;
import com.example.gestionproblemes.security.UtilisateurConnecte;
import com.example.gestionproblemes.service.AffectationService;
import com.example.gestionproblemes.service.ProblemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Cycle de vie des problemes techniques :
 * declaration, consultation, affectation, traitement, resolution et cloture.
 */
@RestController
@RequestMapping("/api/problemes")
@RequiredArgsConstructor
public class ProblemeController {

    private final ProblemeService problemeService;
    private final AffectationService affectationService;

    // ---------------------- Consultation ----------------------

    @GetMapping
    public ResponseEntity<Page<Probleme>> rechercher(
            @RequestParam(required = false) StatutProbleme statut,
            @RequestParam(required = false) Priorite priorite,
            @RequestParam(required = false) Long idCategorie,
            @RequestParam(required = false) Long idTechnicien,
            @RequestParam(required = false) Long idDeclarant,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin,
            @RequestParam(required = false) String motCle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int taille,
            @RequestParam(defaultValue = "dateDeclaration") String tri,
            @RequestParam(defaultValue = "DESC") Sort.Direction sens) {

        return ResponseEntity.ok(problemeService.rechercher(statut, priorite, idCategorie, idTechnicien,
                idDeclarant, dateDebut, dateFin, motCle, PageRequest.of(page, taille, Sort.by(sens, tri))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Probleme> consulter(@PathVariable Long id) {
        return ResponseEntity.ok(problemeService.consulter(id, UtilisateurConnecte.get()));
    }

    @GetMapping("/mes-declarations")
    public ResponseEntity<List<Probleme>> mesDeclarations() {
        return ResponseEntity.ok(problemeService.mesDeclarations(UtilisateurConnecte.get().getIdUtilisateur()));
    }

    @GetMapping("/mes-affectations")
    public ResponseEntity<List<Probleme>> mesAffectations() {
        return ResponseEntity.ok(problemeService.problemesAssignes(UtilisateurConnecte.get().getIdUtilisateur()));
    }

    // ---------------------- Declaration ----------------------

    @PostMapping
    public ResponseEntity<Probleme> declarer(@RequestBody Map<String, Object> corps) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(problemeService.declarer(corps, UtilisateurConnecte.get()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Probleme> modifier(@PathVariable Long id,
                                             @RequestBody Map<String, Object> corps) {
        return ResponseEntity.ok(problemeService.modifier(id, corps, UtilisateurConnecte.get()));
    }

    // ---------------------- Affectation ----------------------

    @PatchMapping("/{id}/affecter")
    public ResponseEntity<Affectation> affecter(@PathVariable Long id,
                                                @RequestBody Map<String, Object> corps) {
        return ResponseEntity.ok(affectationService.affecter(id, corps, UtilisateurConnecte.get()));
    }

    @PatchMapping("/{id}/reaffecter")
    public ResponseEntity<Affectation> reaffecter(@PathVariable Long id,
                                                  @RequestBody Map<String, Object> corps) {
        return ResponseEntity.ok(affectationService.affecter(id, corps, UtilisateurConnecte.get()));
    }

    @GetMapping("/{id}/affectations")
    public ResponseEntity<List<Affectation>> affectations(@PathVariable Long id) {
        return ResponseEntity.ok(affectationService.historiqueAffectations(id));
    }

    @GetMapping("/{id}/suggestion-technicien")
    public ResponseEntity<Technicien> suggestion(@PathVariable Long id) {
        return ResponseEntity.ok(affectationService.suggestion());
    }

    @PatchMapping("/{id}/priorite")
    public ResponseEntity<Probleme> definirPriorite(@PathVariable Long id,
                                                    @RequestBody Map<String, Object> corps) {
        return ResponseEntity.ok(problemeService.definirPriorite(id, corps, UtilisateurConnecte.get()));
    }

    // ---------------------- Traitement ----------------------

    @PatchMapping("/{id}/prendre-en-charge")
    public ResponseEntity<Probleme> prendreEnCharge(@PathVariable Long id) {
        return ResponseEntity.ok(problemeService.prendreEnCharge(id, UtilisateurConnecte.get()));
    }

    @PatchMapping("/{id}/diagnostic")
    public ResponseEntity<Probleme> diagnostic(@PathVariable Long id,
                                               @RequestBody Map<String, Object> corps) {
        return ResponseEntity.ok(problemeService.enregistrerDiagnostic(id, corps, UtilisateurConnecte.get()));
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<Probleme> changerStatut(@PathVariable Long id,
                                                  @RequestBody Map<String, Object> corps) {
        return ResponseEntity.ok(problemeService.changerStatut(id, corps, UtilisateurConnecte.get()));
    }

    // ---------------------- Resolution et cloture ----------------------

    @PatchMapping("/{id}/resoudre")
    public ResponseEntity<Probleme> resoudre(@PathVariable Long id,
                                             @RequestBody Map<String, Object> corps) {
        return ResponseEntity.ok(problemeService.resoudre(id, corps, UtilisateurConnecte.get()));
    }

    @PatchMapping("/{id}/confirmer")
    public ResponseEntity<Probleme> confirmer(@PathVariable Long id,
                                              @RequestBody(required = false) Map<String, Object> corps) {
        return ResponseEntity.ok(problemeService.confirmerResolution(
                id, corps == null ? Map.of("confirme", true) : corps, UtilisateurConnecte.get()));
    }

    @PatchMapping("/{id}/cloturer")
    public ResponseEntity<Probleme> cloturer(@PathVariable Long id,
                                             @RequestParam(required = false) String motif) {
        return ResponseEntity.ok(problemeService.cloturer(id, motif, UtilisateurConnecte.get()));
    }
}
