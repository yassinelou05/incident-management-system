package com.example.gestionproblemes.repository;

import com.example.gestionproblemes.model.Historique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueRepository extends JpaRepository<Historique, Long> {
    List<Historique> findByProblemeIdProblemeOrderByDateActionAsc(Long idProbleme);
}
