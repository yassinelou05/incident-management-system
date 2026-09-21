package com.example.gestionproblemes.repository;

import com.example.gestionproblemes.model.Commentaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {

    List<Commentaire> findByProblemeIdProblemeOrderByDateCreationAsc(Long idProbleme);

    List<Commentaire> findByProblemeIdProblemeAndInterneFalseOrderByDateCreationAsc(Long idProbleme);
}
