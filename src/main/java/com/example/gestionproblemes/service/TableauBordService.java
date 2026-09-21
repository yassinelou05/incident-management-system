package com.example.gestionproblemes.service;

import com.example.gestionproblemes.enums.Role;
import com.example.gestionproblemes.enums.StatutProbleme;
import com.example.gestionproblemes.model.Probleme;
import com.example.gestionproblemes.model.Utilisateur;
import com.example.gestionproblemes.repository.ProblemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Indicateurs et statistiques du tableau de bord. */
@Service
@RequiredArgsConstructor
public class TableauBordService {

    private static final int NOMBRE_DERNIERS_PROBLEMES = 5;

    private final ProblemeRepository problemeRepository;

    /** Vue globale, destinee au responsable support et a l'administrateur. */
    @Transactional(readOnly = true)
    public Map<String, Object> statistiquesGlobales() {
        long total = problemeRepository.count();
        long resolus = problemeRepository.countByStatut(StatutProbleme.RESOLU);
        long fermes = problemeRepository.countByStatut(StatutProbleme.FERME);

        List<Probleme> derniers = problemeRepository.findAll(
                PageRequest.of(0, NOMBRE_DERNIERS_PROBLEMES,
                        Sort.by(Sort.Direction.DESC, "dateDeclaration"))).getContent();

        Map<String, Object> resultat = new LinkedHashMap<>();
        resultat.put("totalProblemes", total);
        resultat.put("problemesNouveaux", problemeRepository.countByStatut(StatutProbleme.NOUVEAU));
        resultat.put("problemesAssignes", problemeRepository.countByStatut(StatutProbleme.ASSIGNE));
        resultat.put("problemesEnCours", problemeRepository.countByStatut(StatutProbleme.EN_COURS));
        resultat.put("problemesEnAttente", problemeRepository.countByStatut(StatutProbleme.EN_ATTENTE));
        resultat.put("problemesResolus", resolus);
        resultat.put("problemesFermes", fermes);
        resultat.put("tempsMoyenResolutionHeures", calculerTempsMoyenResolution());
        resultat.put("tauxResolution", total == 0 ? 0d
                : Math.round((resolus + fermes) * 10000.0 / total) / 100.0);
        resultat.put("repartitionParCategorie", versMap(problemeRepository.compterParCategorie()));
        resultat.put("repartitionParTechnicien", versMap(problemeRepository.compterParTechnicien()));
        resultat.put("repartitionParPriorite", versMap(problemeRepository.compterParPriorite()));
        resultat.put("repartitionParStatut", versMap(problemeRepository.compterParStatut()));
        resultat.put("derniersProblemes", derniers);
        return resultat;
    }

    /** Vue restreinte au perimetre de l'utilisateur connecte. */
    @Transactional(readOnly = true)
    public Map<String, Object> statistiquesPersonnelles(Utilisateur utilisateur) {
        List<Probleme> problemes;
        if (utilisateur.getRole() == Role.EMPLOYE) {
            problemes = problemeRepository
                    .findByDeclarantIdUtilisateurOrderByDateDeclarationDesc(utilisateur.getIdUtilisateur());
        } else if (utilisateur.getRole() == Role.TECHNICIEN) {
            problemes = problemeRepository.findAssignesA(utilisateur.getIdUtilisateur());
        } else {
            return statistiquesGlobales();
        }

        Map<String, Long> parStatut = new LinkedHashMap<>();
        for (StatutProbleme statut : StatutProbleme.values()) {
            parStatut.put(statut.name(), problemes.stream().filter(p -> p.getStatut() == statut).count());
        }

        Map<String, Long> parCategorie = new LinkedHashMap<>();
        problemes.forEach(p -> parCategorie.merge(
                p.getCategorie() == null ? "Non classe" : p.getCategorie().getNom(), 1L, Long::sum));

        Map<String, Long> parPriorite = new LinkedHashMap<>();
        problemes.forEach(p -> parPriorite.merge(p.getPriorite().name(), 1L, Long::sum));

        long resolus = parStatut.getOrDefault(StatutProbleme.RESOLU.name(), 0L);
        long fermes = parStatut.getOrDefault(StatutProbleme.FERME.name(), 0L);
        long total = problemes.size();

        List<Probleme> derniers = problemes.stream()
                .sorted((a, b) -> b.getDateDeclaration().compareTo(a.getDateDeclaration()))
                .limit(NOMBRE_DERNIERS_PROBLEMES)
                .toList();

        Map<String, Object> resultat = new LinkedHashMap<>();
        resultat.put("totalProblemes", total);
        resultat.put("problemesNouveaux", parStatut.getOrDefault(StatutProbleme.NOUVEAU.name(), 0L));
        resultat.put("problemesAssignes", parStatut.getOrDefault(StatutProbleme.ASSIGNE.name(), 0L));
        resultat.put("problemesEnCours", parStatut.getOrDefault(StatutProbleme.EN_COURS.name(), 0L));
        resultat.put("problemesEnAttente", parStatut.getOrDefault(StatutProbleme.EN_ATTENTE.name(), 0L));
        resultat.put("problemesResolus", resolus);
        resultat.put("problemesFermes", fermes);
        resultat.put("tempsMoyenResolutionHeures", moyenneHeures(problemes));
        resultat.put("tauxResolution", total == 0 ? 0d
                : Math.round((resolus + fermes) * 10000.0 / total) / 100.0);
        resultat.put("repartitionParCategorie", parCategorie);
        resultat.put("repartitionParTechnicien", Map.of());
        resultat.put("repartitionParPriorite", parPriorite);
        resultat.put("repartitionParStatut", parStatut);
        resultat.put("derniersProblemes", derniers);
        return resultat;
    }

    // ------------------------------------------------------------------

    private Double calculerTempsMoyenResolution() {
        List<Object[]> lignes = problemeRepository.datesResolution();
        double total = 0;
        int compte = 0;
        for (Object[] ligne : lignes) {
            LocalDateTime declaration = (LocalDateTime) ligne[0];
            LocalDateTime resolution = (LocalDateTime) ligne[1];
            if (declaration != null && resolution != null) {
                total += Duration.between(declaration, resolution).toMinutes() / 60.0;
                compte++;
            }
        }
        return compte == 0 ? null : Math.round(total / compte * 100.0) / 100.0;
    }

    private Double moyenneHeures(List<Probleme> problemes) {
        List<Probleme> resolus = problemes.stream().filter(p -> p.getDateResolution() != null).toList();
        if (resolus.isEmpty()) {
            return null;
        }
        double total = resolus.stream()
                .mapToDouble(p -> Duration.between(p.getDateDeclaration(), p.getDateResolution()).toMinutes() / 60.0)
                .sum();
        return Math.round(total / resolus.size() * 100.0) / 100.0;
    }

    private Map<String, Long> versMap(List<Object[]> lignes) {
        Map<String, Long> resultat = new LinkedHashMap<>();
        for (Object[] ligne : lignes) {
            String cle = ligne[0] == null ? "Non defini" : ligne[0].toString();
            resultat.put(cle, ((Number) ligne[1]).longValue());
        }
        return resultat;
    }
}
