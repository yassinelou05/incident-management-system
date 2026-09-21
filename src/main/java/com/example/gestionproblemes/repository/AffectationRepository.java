package com.example.gestionproblemes.repository;

import com.example.gestionproblemes.model.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AffectationRepository extends JpaRepository<Affectation, Long> {

    List<Affectation> findByProblemeIdProblemeOrderByDateAffectationDesc(Long idProbleme);

    @Query("SELECT a FROM Affectation a WHERE a.probleme.idProbleme = :idProbleme AND a.active = true")
    Optional<Affectation> findActiveByProbleme(@Param("idProbleme") Long idProbleme);

    @Query("SELECT COUNT(a) > 0 FROM Affectation a "
         + "WHERE a.probleme.idProbleme = :idProbleme "
         + "AND a.technicien.idUtilisateur = :idTechnicien AND a.active = true")
    boolean estAssigneA(@Param("idProbleme") Long idProbleme, @Param("idTechnicien") Long idTechnicien);
}
