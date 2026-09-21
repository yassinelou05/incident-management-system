package com.example.gestionproblemes.controller;

import com.example.gestionproblemes.model.Commentaire;
import com.example.gestionproblemes.security.UtilisateurConnecte;
import com.example.gestionproblemes.service.CommentaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Commentaires attaches aux problemes. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentaireController {

    private final CommentaireService commentaireService;

    @GetMapping("/problemes/{idProbleme}/commentaires")
    public ResponseEntity<List<Commentaire>> lister(@PathVariable Long idProbleme) {
        return ResponseEntity.ok(commentaireService.lister(idProbleme, UtilisateurConnecte.get()));
    }

    @PostMapping("/problemes/{idProbleme}/commentaires")
    public ResponseEntity<Commentaire> ajouter(@PathVariable Long idProbleme,
                                               @RequestBody Map<String, Object> corps) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentaireService.ajouter(idProbleme, corps, UtilisateurConnecte.get()));
    }

    @PutMapping("/commentaires/{id}")
    public ResponseEntity<Commentaire> modifier(@PathVariable Long id,
                                                @RequestBody Map<String, Object> corps) {
        return ResponseEntity.ok(commentaireService.modifier(id, corps, UtilisateurConnecte.get()));
    }

    @DeleteMapping("/commentaires/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        commentaireService.supprimer(id, UtilisateurConnecte.get());
        return ResponseEntity.noContent().build();
    }
}
