package com.example.gestionproblemes.service;

import com.example.gestionproblemes.enums.StatutCompte;
import com.example.gestionproblemes.enums.StatutProbleme;
import com.example.gestionproblemes.enums.TypeAction;
import com.example.gestionproblemes.enums.TypeNotification;
import com.example.gestionproblemes.model.Affectation;
import com.example.gestionproblemes.model.Probleme;
import com.example.gestionproblemes.model.Technicien;
import com.example.gestionproblemes.model.Utilisateur;
import com.example.gestionproblemes.repository.AffectationRepository;
import com.example.gestionproblemes.repository.ProblemeRepository;
import com.example.gestionproblemes.repository.TechnicienRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AffectationService {

    private final AffectationRepository affectationRepository;
    private final ProblemeRepository problemeRepository;
    private final TechnicienRepository technicienRepository;
    private final ProblemeService problemeService;
    private final HistoriqueService historiqueService;
    private final NotificationService notificationService;

    @Transactional
    public Affectation affecter(Long idProbleme, Map<String, Object> corps, Utilisateur demandeur) {
        Probleme probleme = problemeService.charger(idProbleme);
        problemeService.verifierModifiable(probleme);

        Long idTechnicien = RequeteUtils.identifiantObligatoire(corps, "idTechnicien", "technicien");
        String note = RequeteUtils.texte(corps, "noteAffectation");
        Technicien technicien = chargerTechnicien(idTechnicien);

        Optional<Affectation> affectationActive = affectationRepository.findActiveByProbleme(idProbleme);
        if (affectationActive.isPresent()
                && affectationActive.get().getTechnicien().getIdUtilisateur().equals(idTechnicien)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le probleme est deja assigne a ce technicien.");
        }

        affectationActive.ifPresent(ancienne -> {
            ancienne.cloturerAffectation();
            ancienne.getTechnicien().decrementerCharge();
            affectationRepository.save(ancienne);
        });

        Affectation affectation = affectationRepository.save(Affectation.builder()
                .probleme(probleme)
                .technicien(technicien)
                .affectePar(demandeur)
                .noteAffectation(note)
                .dateAffectation(LocalDateTime.now())
                .active(Boolean.TRUE)
                .build());

        technicien.incrementerCharge();
        technicienRepository.save(technicien);

        StatutProbleme ancienStatut = probleme.getStatut();
        probleme.setStatut(StatutProbleme.ASSIGNE);
        probleme.setDateAffectation(LocalDateTime.now());
        problemeRepository.save(probleme);

        boolean reaffectation = affectationActive.isPresent();
        historiqueService.enregistrer(probleme, TypeAction.AFFECTATION, ancienStatut, StatutProbleme.ASSIGNE,
                (reaffectation ? "Reaffectation a " : "Affectation a ") + technicien.getNomComplet()
                        + (note == null || note.isBlank() ? "" : " - " + note),
                demandeur);

        notificationService.notifier(technicien, TypeNotification.AFFECTATION,
                "Un probleme vous a ete assigne : " + probleme.getReference() + " - " + probleme.getTitre(),
                probleme);

        notificationService.notifier(probleme.getDeclarant(), TypeNotification.AFFECTATION,
                "Votre probleme " + probleme.getReference() + " a ete affecte a un technicien.", probleme);

        log.info("Probleme {} affecte a {} par {}", probleme.getReference(),
                technicien.getEmail(), demandeur.getEmail());

        return affectation;
    }

    @Transactional(readOnly = true)
    public List<Affectation> historiqueAffectations(Long idProbleme) {
        return affectationRepository.findByProblemeIdProblemeOrderByDateAffectationDesc(idProbleme);
    }

    /** Propose le technicien actif le moins charge : aide a la decision du responsable. */
    @Transactional(readOnly = true)
    public Technicien suggestion() {
        return technicienRepository.findDisponiblesParCharge().stream()
                .filter(t -> t.getStatutCompte() == StatutCompte.ACTIF)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Aucun technicien disponible actuellement."));
    }

    private Technicien chargerTechnicien(Long idTechnicien) {
        Technicien technicien = technicienRepository.findById(idTechnicien)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Technicien introuvable."));
        if (technicien.getStatutCompte() != StatutCompte.ACTIF) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le compte de ce technicien n'est pas actif.");
        }
        if (Boolean.FALSE.equals(technicien.getDisponible())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ce technicien est actuellement indisponible.");
        }
        return technicien;
    }
}
