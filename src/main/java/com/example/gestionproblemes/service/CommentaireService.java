package com.example.gestionproblemes.service;

import com.example.gestionproblemes.enums.Role;
import com.example.gestionproblemes.enums.TypeAction;
import com.example.gestionproblemes.enums.TypeNotification;
import com.example.gestionproblemes.model.Commentaire;
import com.example.gestionproblemes.model.Probleme;
import com.example.gestionproblemes.model.Technicien;
import com.example.gestionproblemes.model.Utilisateur;
import com.example.gestionproblemes.repository.CommentaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/** Fil de discussion attache a un probleme. */
@Service
@RequiredArgsConstructor
public class CommentaireService {

    private final CommentaireRepository commentaireRepository;
    private final ProblemeService problemeService;
    private final HistoriqueService historiqueService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<Commentaire> lister(Long idProbleme, Utilisateur demandeur) {
        Probleme probleme = problemeService.charger(idProbleme);
        problemeService.verifierDroitDeLecture(probleme, demandeur);

        return demandeur.getRole() == Role.EMPLOYE
                ? commentaireRepository.findByProblemeIdProblemeAndInterneFalseOrderByDateCreationAsc(idProbleme)
                : commentaireRepository.findByProblemeIdProblemeOrderByDateCreationAsc(idProbleme);
    }

    @Transactional
    public Commentaire ajouter(Long idProbleme, Map<String, Object> corps, Utilisateur auteur) {
        Probleme probleme = problemeService.charger(idProbleme);
        problemeService.verifierDroitDeLecture(probleme, auteur);
        problemeService.verifierModifiable(probleme);

        String contenu = RequeteUtils.texteObligatoire(corps, "contenu", "contenu");
        boolean interne = Boolean.TRUE.equals(RequeteUtils.booleen(corps, "interne", Boolean.FALSE))
                && auteur.getRole() != Role.EMPLOYE;

        Commentaire commentaire = commentaireRepository.save(Commentaire.builder()
                .probleme(probleme)
                .auteur(auteur)
                .contenu(contenu)
                .interne(interne)
                .build());

        historiqueService.enregistrer(probleme, TypeAction.COMMENTAIRE,
                "Commentaire ajoute par " + auteur.getNomComplet(), auteur);

        notifierAutresIntervenants(probleme, auteur, interne);
        return commentaire;
    }

    @Transactional
    public Commentaire modifier(Long idCommentaire, Map<String, Object> corps, Utilisateur demandeur) {
        Commentaire commentaire = charger(idCommentaire);
        if (!commentaire.getAuteur().getIdUtilisateur().equals(demandeur.getIdUtilisateur())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Seul l'auteur peut modifier son commentaire.");
        }
        problemeService.verifierModifiable(commentaire.getProbleme());
        commentaire.modifier(RequeteUtils.texteObligatoire(corps, "contenu", "contenu"));
        return commentaireRepository.save(commentaire);
    }

    @Transactional
    public void supprimer(Long idCommentaire, Utilisateur demandeur) {
        Commentaire commentaire = charger(idCommentaire);
        boolean estAuteur = commentaire.getAuteur().getIdUtilisateur().equals(demandeur.getIdUtilisateur());
        if (!estAuteur && demandeur.getRole() != Role.ADMINISTRATEUR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Seul l'auteur ou un administrateur peut supprimer ce commentaire.");
        }
        commentaireRepository.delete(commentaire);
    }

    private Commentaire charger(Long id) {
        return commentaireRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commentaire introuvable."));
    }

    private void notifierAutresIntervenants(Probleme probleme, Utilisateur auteur, boolean interne) {
        String message = "Nouveau commentaire sur le probleme " + probleme.getReference();

        Technicien technicien = probleme.getTechnicienAssigne();
        if (technicien != null && !technicien.getIdUtilisateur().equals(auteur.getIdUtilisateur())) {
            notificationService.notifier(technicien, TypeNotification.COMMENTAIRE, message, probleme);
        }
        if (!interne && !probleme.getDeclarant().getIdUtilisateur().equals(auteur.getIdUtilisateur())) {
            notificationService.notifier(probleme.getDeclarant(), TypeNotification.COMMENTAIRE, message, probleme);
        }
    }
}
