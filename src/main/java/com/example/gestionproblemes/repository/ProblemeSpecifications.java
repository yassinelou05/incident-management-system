package com.example.gestionproblemes.repository;

import com.example.gestionproblemes.enums.Priorite;
import com.example.gestionproblemes.enums.StatutProbleme;
import com.example.gestionproblemes.model.Affectation;
import com.example.gestionproblemes.model.Probleme;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Criteres de recherche dynamiques de l'ecran "Liste des problemes". */
public final class ProblemeSpecifications {

    private ProblemeSpecifications() {
    }

    public static Specification<Probleme> filtrer(StatutProbleme statut,
                                                  Priorite priorite,
                                                  Long idCategorie,
                                                  Long idTechnicien,
                                                  Long idDeclarant,
                                                  LocalDateTime dateDebut,
                                                  LocalDateTime dateFin,
                                                  String motCle) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (statut != null) {
                predicates.add(cb.equal(root.get("statut"), statut));
            }
            if (priorite != null) {
                predicates.add(cb.equal(root.get("priorite"), priorite));
            }
            if (idCategorie != null) {
                predicates.add(cb.equal(root.get("categorie").get("idCategorie"), idCategorie));
            }
            if (idDeclarant != null) {
                predicates.add(cb.equal(root.get("declarant").get("idUtilisateur"), idDeclarant));
            }
            if (idTechnicien != null) {
                Join<Probleme, Affectation> affectation = root.join("affectations", JoinType.INNER);
                predicates.add(cb.and(
                        cb.equal(affectation.get("technicien").get("idUtilisateur"), idTechnicien),
                        cb.isTrue(affectation.get("active"))
                ));
                if (query != null) {
                    query.distinct(true);
                }
            }
            if (dateDebut != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dateDeclaration"), dateDebut));
            }
            if (dateFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dateDeclaration"), dateFin));
            }
            if (motCle != null && !motCle.isBlank()) {
                String pattern = "%" + motCle.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("titre")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("reference")), pattern)
                ));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
