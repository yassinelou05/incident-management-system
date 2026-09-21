package com.example.gestionproblemes.repository;

import com.example.gestionproblemes.model.Technicien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicienRepository extends JpaRepository<Technicien, Long> {

    Optional<Technicien> findByEmailIgnoreCase(String email);

    List<Technicien> findByDisponibleTrue();

    /** Techniciens disponibles, tries par charge de travail croissante. */
    @Query("SELECT t FROM Technicien t WHERE t.disponible = true "
         + "ORDER BY t.chargeTravail ASC, t.niveauExpertise DESC")
    List<Technicien> findDisponiblesParCharge();
}
