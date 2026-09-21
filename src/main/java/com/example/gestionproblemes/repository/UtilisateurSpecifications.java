package com.example.gestionproblemes.repository;

import com.example.gestionproblemes.enums.Role;
import com.example.gestionproblemes.enums.StatutCompte;
import com.example.gestionproblemes.model.Utilisateur;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/** Criteres de recherche de l'ecran "Gestion des utilisateurs". */
public final class UtilisateurSpecifications {

    private UtilisateurSpecifications() {
    }

    public static Specification<Utilisateur> filtrer(Role role, StatutCompte statut,
                                                     Long idDepartement, String motCle) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (role != null) {
                predicates.add(cb.equal(root.get("role"), role));
            }
            if (statut != null) {
                predicates.add(cb.equal(root.get("statutCompte"), statut));
            }
            if (idDepartement != null) {
                predicates.add(cb.equal(root.get("departement").get("idDepartement"), idDepartement));
            }
            if (motCle != null && !motCle.isBlank()) {
                String pattern = "%" + motCle.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nom")), pattern),
                        cb.like(cb.lower(root.get("prenom")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern)
                ));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
