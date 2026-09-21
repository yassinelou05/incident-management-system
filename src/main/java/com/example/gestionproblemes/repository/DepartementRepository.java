package com.example.gestionproblemes.repository;

import com.example.gestionproblemes.model.Departement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartementRepository extends JpaRepository<Departement, Long> {
    Optional<Departement> findByNomIgnoreCase(String nom);
    boolean existsByNomIgnoreCase(String nom);
}
