package com.example.gestionproblemes.repository;

import com.example.gestionproblemes.enums.StatutProbleme;
import com.example.gestionproblemes.model.Probleme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemeRepository extends JpaRepository<Probleme, Long>,
                                            JpaSpecificationExecutor<Probleme> {

    Optional<Probleme> findByReference(String reference);

    List<Probleme> findByDeclarantIdUtilisateurOrderByDateDeclarationDesc(Long idDeclarant);

    long countByStatut(StatutProbleme statut);

    /** Compteur servant a generer la reference annuelle du probleme. */
    @Query("SELECT COUNT(p) FROM Probleme p WHERE p.dateDeclaration >= :debutAnnee")
    long compterDepuis(@Param("debutAnnee") LocalDateTime debutAnnee);

    /** Problemes actuellement assignes a un technicien via l'affectation active. */
    @Query("SELECT DISTINCT p FROM Probleme p JOIN p.affectations a "
         + "WHERE a.technicien.idUtilisateur = :idTechnicien AND a.active = true "
         + "ORDER BY p.dateDeclaration ASC")
    List<Probleme> findAssignesA(@Param("idTechnicien") Long idTechnicien);

    // ------------------- Indicateurs du tableau de bord -------------------

    @Query("SELECT p.categorie.nom, COUNT(p) FROM Probleme p GROUP BY p.categorie.nom ORDER BY COUNT(p) DESC")
    List<Object[]> compterParCategorie();

    @Query("SELECT p.statut, COUNT(p) FROM Probleme p GROUP BY p.statut")
    List<Object[]> compterParStatut();

    @Query("SELECT p.priorite, COUNT(p) FROM Probleme p GROUP BY p.priorite")
    List<Object[]> compterParPriorite();

    @Query("SELECT CONCAT(a.technicien.prenom, ' ', a.technicien.nom), COUNT(DISTINCT p) "
         + "FROM Probleme p JOIN p.affectations a WHERE a.active = true "
         + "GROUP BY a.technicien.idUtilisateur, a.technicien.prenom, a.technicien.nom "
         + "ORDER BY COUNT(DISTINCT p) DESC")
    List<Object[]> compterParTechnicien();

    /** Dates servant au calcul du temps moyen de resolution, effectue en Java. */
    @Query("SELECT p.dateDeclaration, p.dateResolution FROM Probleme p WHERE p.dateResolution IS NOT NULL")
    List<Object[]> datesResolution();
}
