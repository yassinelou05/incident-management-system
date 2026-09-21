package com.example.gestionproblemes.service;

import com.example.gestionproblemes.enums.*;
import com.example.gestionproblemes.model.*;
import com.example.gestionproblemes.repository.AffectationRepository;
import com.example.gestionproblemes.repository.CommentaireRepository;
import com.example.gestionproblemes.repository.ProblemeRepository;
import com.example.gestionproblemes.repository.ProblemeSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemeService {

    private final ProblemeRepository problemeRepository;
    private final AffectationRepository affectationRepository;
    private final CommentaireRepository commentaireRepository;
    private final CategorieService categorieService;
    private final HistoriqueService historiqueService;
    private final NotificationService notificationService;


    @Transactional(readOnly = true)
    public Page<Probleme> rechercher(StatutProbleme statut, Priorite priorite, Long idCategorie,
                                     Long idTechnicien, Long idDeclarant,
                                     LocalDateTime dateDebut, LocalDateTime dateFin,
                                     String motCle, Pageable pageable) {
        return problemeRepository.findAll(
                ProblemeSpecifications.filtrer(statut, priorite, idCategorie, idTechnicien,
                        idDeclarant, dateDebut, dateFin, motCle),
                pageable);
    }

    @Transactional(readOnly = true)
    public Probleme charger(Long id) {
        return problemeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Probleme introuvable (identifiant : " + id + ")."));
    }

    @Transactional(readOnly = true)
    public Probleme consulter(Long id, Utilisateur demandeur) {
        Probleme probleme = charger(id);
        verifierDroitDeLecture(probleme, demandeur);
        if (demandeur.getRole() == Role.EMPLOYE) {
            probleme.setCommentaires(
                    commentaireRepository.findByProblemeIdProblemeAndInterneFalseOrderByDateCreationAsc(id));
        }
        return probleme;
    }

    @Transactional(readOnly = true)
    public List<Probleme> mesDeclarations(Long idDeclarant) {
        return problemeRepository.findByDeclarantIdUtilisateurOrderByDateDeclarationDesc(idDeclarant);
    }

    @Transactional(readOnly = true)
    public List<Probleme> problemesAssignes(Long idTechnicien) {
        return problemeRepository.findAssignesA(idTechnicien);
    }


    @Transactional
    public Probleme declarer(Map<String, Object> corps, Utilisateur declarant) {

        String titre = RequeteUtils.texteObligatoire(corps, "titre", "titre");
        String description = RequeteUtils.texteObligatoire(corps, "description", "description");
        Long idCategorie = RequeteUtils.identifiantObligatoire(corps, "idCategorie", "categorie");
        Priorite priorite = RequeteUtils.enumeration(corps, "priorite", Priorite.class, "priorite", true);

        Categorie categorie = categorieService.charger(idCategorie);
        if (Boolean.FALSE.equals(categorie.getActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La categorie selectionnee est desactivee.");
        }

        Probleme probleme = Probleme.builder()
                .reference(genererReference())
                .titre(titre)
                .description(description)
                .equipementConcerne(RequeteUtils.texte(corps, "equipementConcerne"))
                .categorie(categorie)
                .priorite(priorite)
                .statut(StatutProbleme.NOUVEAU)
                .declarant(declarant)
                .dateDeclaration(LocalDateTime.now())
                .resolutionConfirmee(Boolean.FALSE)
                .build();

        Probleme enregistre = problemeRepository.save(probleme);

        historiqueService.enregistrer(enregistre, TypeAction.CREATION, null, StatutProbleme.NOUVEAU,
                "Declaration du probleme " + enregistre.getReference(), declarant);

        notificationService.notifierSuperviseurs(TypeNotification.CHANGEMENT_STATUT,
                "Nouveau probleme declare : " + enregistre.getReference() + " - " + enregistre.getTitre(),
                enregistre);

        log.info("Probleme {} declare par {}", enregistre.getReference(), declarant.getEmail());
        return enregistre;
    }

    @Transactional
    public Probleme modifier(Long id, Map<String, Object> corps, Utilisateur demandeur) {
        Probleme probleme = charger(id);
        verifierModifiable(probleme);

        boolean estDeclarant = estDeclarant(probleme, demandeur);
        boolean estSuperviseur = demandeur.getRole() == Role.RESPONSABLE_SUPPORT
                || demandeur.getRole() == Role.ADMINISTRATEUR;

        if (!estDeclarant && !estSuperviseur) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Seul le declarant ou un responsable peut modifier ce probleme.");
        }
        if (estDeclarant && !estSuperviseur && probleme.getStatut() != StatutProbleme.NOUVEAU) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le probleme ne peut plus etre modifie : il est deja pris en charge.");
        }

        probleme.setTitre(RequeteUtils.texteObligatoire(corps, "titre", "titre"));
        probleme.setDescription(RequeteUtils.texteObligatoire(corps, "description", "description"));
        probleme.setEquipementConcerne(RequeteUtils.texte(corps, "equipementConcerne"));
        probleme.setCategorie(categorieService.charger(
                RequeteUtils.identifiantObligatoire(corps, "idCategorie", "categorie")));
        probleme.setPriorite(RequeteUtils.enumeration(corps, "priorite", Priorite.class, "priorite", true));

        return problemeRepository.save(probleme);
    }


    @Transactional
    public Probleme definirPriorite(Long id, Map<String, Object> corps, Utilisateur demandeur) {
        Probleme probleme = charger(id);
        verifierModifiable(probleme);

        Priorite nouvelle = RequeteUtils.enumeration(corps, "priorite", Priorite.class, "priorite", true);
        String motif = RequeteUtils.texte(corps, "motif");
        Priorite ancienne = probleme.getPriorite();

        probleme.setPriorite(nouvelle);
        Probleme enregistre = problemeRepository.save(probleme);

        historiqueService.enregistrer(enregistre, TypeAction.CHANGEMENT_PRIORITE,
                "Priorite : " + ancienne + " -> " + nouvelle + (motif == null ? "" : " (" + motif + ")"),
                demandeur);

        return enregistre;
    }

    // ==================================================================
    //  Traitement
    // ==================================================================

    /** Le technicien prend en charge le probleme : ASSIGNE ou EN_ATTENTE vers EN_COURS. */
    @Transactional
    public Probleme prendreEnCharge(Long id, Utilisateur demandeur) {
        Probleme probleme = charger(id);
        verifierTechnicienAssigne(probleme, demandeur);
        verifierModifiable(probleme);

        StatutProbleme ancien = probleme.getStatut();
        if (ancien != StatutProbleme.ASSIGNE && ancien != StatutProbleme.EN_ATTENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le probleme doit etre assigne ou en attente pour etre pris en charge.");
        }

        probleme.setStatut(StatutProbleme.EN_COURS);
        if (probleme.getDatePriseEnCharge() == null) {
            probleme.setDatePriseEnCharge(LocalDateTime.now());
        }
        Probleme enregistre = problemeRepository.save(probleme);

        historiqueService.enregistrer(enregistre, TypeAction.CHANGEMENT_STATUT, ancien, StatutProbleme.EN_COURS,
                "Prise en charge par " + demandeur.getNomComplet(), demandeur);

        notificationService.notifier(enregistre.getDeclarant(), TypeNotification.CHANGEMENT_STATUT,
                "Votre probleme " + enregistre.getReference() + " est en cours de traitement.", enregistre);

        return enregistre;
    }

    /** Le technicien ne peut agir que sur les problemes qui lui sont assignes. */
    @Transactional
    public Probleme enregistrerDiagnostic(Long id, Map<String, Object> corps, Utilisateur demandeur) {
        Probleme probleme = charger(id);
        verifierTechnicienAssigne(probleme, demandeur);
        verifierModifiable(probleme);

        String diagnostic = RequeteUtils.texteObligatoire(corps, "diagnostic", "diagnostic");
        StatutProbleme ancien = probleme.getStatut();
        probleme.setDiagnostic(diagnostic);

        if (ancien == StatutProbleme.ASSIGNE) {
            probleme.setStatut(StatutProbleme.EN_COURS);
            if (probleme.getDatePriseEnCharge() == null) {
                probleme.setDatePriseEnCharge(LocalDateTime.now());
            }
        }

        Probleme enregistre = problemeRepository.save(probleme);
        historiqueService.enregistrer(enregistre, TypeAction.CHANGEMENT_STATUT, ancien, enregistre.getStatut(),
                "Diagnostic saisi", demandeur);

        return enregistre;
    }

    /** Changement de statut generique, controle par la machine a etats de StatutProbleme. */
    @Transactional
    public Probleme changerStatut(Long id, Map<String, Object> corps, Utilisateur demandeur) {
        Probleme probleme = charger(id);
        verifierModifiable(probleme);

        if (demandeur.getRole() == Role.TECHNICIEN) {
            verifierTechnicienAssigne(probleme, demandeur);
        }

        StatutProbleme cible = RequeteUtils.enumeration(corps, "statut", StatutProbleme.class, "statut", true);
        String motif = RequeteUtils.texte(corps, "motif");
        StatutProbleme ancien = probleme.getStatut();

        if (cible == StatutProbleme.RESOLU) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Utilisez l'operation de resolution afin de renseigner la solution appliquee.");
        }
        if (!ancien.peutPasserA(cible)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Transition de statut interdite : " + ancien + " -> " + cible);
        }

        probleme.setStatut(cible);
        Probleme enregistre = problemeRepository.save(probleme);

        historiqueService.enregistrer(enregistre, TypeAction.CHANGEMENT_STATUT, ancien, cible, motif, demandeur);

        notificationService.notifier(enregistre.getDeclarant(), TypeNotification.CHANGEMENT_STATUT,
                "Statut du probleme " + enregistre.getReference() + " : " + cible, enregistre);

        return enregistre;
    }

    // ==================================================================
    //  Resolution et cloture
    // ==================================================================

    /** La solution appliquee est obligatoire pour marquer un probleme comme resolu. */
    @Transactional
    public Probleme resoudre(Long id, Map<String, Object> corps, Utilisateur demandeur) {
        Probleme probleme = charger(id);
        verifierModifiable(probleme);

        if (demandeur.getRole() == Role.TECHNICIEN) {
            verifierTechnicienAssigne(probleme, demandeur);
        }

        String solution = RequeteUtils.texteObligatoire(corps, "solution", "solution appliquee");

        if (!probleme.getStatut().peutPasserA(StatutProbleme.RESOLU)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le probleme ne peut pas etre resolu depuis le statut " + probleme.getStatut() + ".");
        }

        StatutProbleme ancien = probleme.getStatut();
        probleme.setSolution(solution);
        probleme.setStatut(StatutProbleme.RESOLU);
        probleme.setDateResolution(LocalDateTime.now());
        Probleme enregistre = problemeRepository.save(probleme);

        historiqueService.enregistrer(enregistre, TypeAction.RESOLUTION, ancien, StatutProbleme.RESOLU,
                "Solution appliquee par " + demandeur.getNomComplet(), demandeur);

        notificationService.notifier(enregistre.getDeclarant(), TypeNotification.RESOLUTION,
                "Le probleme " + enregistre.getReference() + " a ete resolu. Merci de confirmer la resolution.",
                enregistre);

        return enregistre;
    }

    /** Confirmation ou refus de la resolution par le declarant. */
    @Transactional
    public Probleme confirmerResolution(Long id, Map<String, Object> corps, Utilisateur demandeur) {
        Probleme probleme = charger(id);

        if (!estDeclarant(probleme, demandeur)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Seul le declarant peut confirmer la resolution.");
        }
        if (probleme.getStatut() != StatutProbleme.RESOLU) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le probleme n'est pas au statut RESOLU.");
        }

        boolean confirme = Boolean.TRUE.equals(RequeteUtils.booleen(corps, "confirme", Boolean.TRUE));
        String commentaire = RequeteUtils.texte(corps, "commentaire");

        if (confirme) {
            probleme.setResolutionConfirmee(Boolean.TRUE);
            Probleme enregistre = problemeRepository.save(probleme);
            historiqueService.enregistrer(enregistre, TypeAction.RESOLUTION,
                    "Resolution confirmee par le declarant", demandeur);
            notificationService.notifierSuperviseurs(TypeNotification.RESOLUTION,
                    "Resolution confirmee pour " + enregistre.getReference() + " : cloture possible.", enregistre);
            return enregistre;
        }

        probleme.setResolutionConfirmee(Boolean.FALSE);
        probleme.setStatut(StatutProbleme.EN_COURS);
        probleme.setDateResolution(null);
        Probleme enregistre = problemeRepository.save(probleme);

        historiqueService.enregistrer(enregistre, TypeAction.REOUVERTURE,
                StatutProbleme.RESOLU, StatutProbleme.EN_COURS,
                "Resolution refusee par le declarant" + (commentaire == null ? "" : " : " + commentaire),
                demandeur);

        notificationService.notifier(enregistre.getTechnicienAssigne(), TypeNotification.CHANGEMENT_STATUT,
                "Le declarant a refuse la resolution du probleme " + enregistre.getReference() + ".", enregistre);

        return enregistre;
    }

    /** La cloture rend le probleme definitivement non modifiable. */
    @Transactional
    public Probleme cloturer(Long id, String motif, Utilisateur demandeur) {
        Probleme probleme = charger(id);

        if (probleme.getStatut() == StatutProbleme.FERME) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le probleme est deja cloture.");
        }

        StatutProbleme ancien = probleme.getStatut();
        probleme.setStatut(StatutProbleme.FERME);
        probleme.setDateCloture(LocalDateTime.now());

        affectationRepository.findActiveByProbleme(id).ifPresent(affectation -> {
            affectation.cloturerAffectation();
            affectation.getTechnicien().decrementerCharge();
            affectationRepository.save(affectation);
        });

        Probleme enregistre = problemeRepository.save(probleme);

        historiqueService.enregistrer(enregistre, TypeAction.CLOTURE, ancien, StatutProbleme.FERME,
                motif == null || motif.isBlank() ? "Cloture du probleme" : motif, demandeur);

        notificationService.notifier(enregistre.getDeclarant(), TypeNotification.CLOTURE,
                "Le probleme " + enregistre.getReference() + " a ete cloture.", enregistre);

        return enregistre;
    }


    public void verifierModifiable(Probleme probleme) {
        if (!probleme.isModifiable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le probleme " + probleme.getReference() + " est cloture et n'est plus modifiable.");
        }
    }

    /** Perimetre d'intervention du technicien. */
    public void verifierTechnicienAssigne(Probleme probleme, Utilisateur demandeur) {
        if (demandeur.getRole() != Role.TECHNICIEN) {
            return;
        }
        if (!affectationRepository.estAssigneA(probleme.getIdProbleme(), demandeur.getIdUtilisateur())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Ce probleme ne vous est pas assigne : vous ne pouvez pas le traiter.");
        }
    }

    public void verifierDroitDeLecture(Probleme probleme, Utilisateur demandeur) {
        switch (demandeur.getRole()) {
            case ADMINISTRATEUR, RESPONSABLE_SUPPORT -> {
                // acces complet
            }
            case TECHNICIEN -> {
                // Le technicien accede aux problemes qui lui sont assignes,
                // ainsi qu'a ceux qu'il a lui-meme declares.
                if (!estDeclarant(probleme, demandeur)) {
                    verifierTechnicienAssigne(probleme, demandeur);
                }
            }
            case EMPLOYE -> {
                if (!estDeclarant(probleme, demandeur)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Vous ne pouvez consulter que vos propres declarations.");
                }
            }
        }
    }

    /** Vrai si l'utilisateur est l'auteur de la declaration. */
    public boolean estDeclarant(Probleme probleme, Utilisateur utilisateur) {
        return probleme.getDeclarant() != null
                && probleme.getDeclarant().getIdUtilisateur().equals(utilisateur.getIdUtilisateur());
    }

    /** Reference lisible de la forme PRB-2026-000042. */
    private String genererReference() {
        int annee = LocalDateTime.now().getYear();
        LocalDateTime debutAnnee = LocalDateTime.of(annee, Month.JANUARY, 1, 0, 0);
        long sequence = problemeRepository.compterDepuis(debutAnnee) + 1;
        return String.format("PRB-%d-%06d", annee, sequence);
    }
}
