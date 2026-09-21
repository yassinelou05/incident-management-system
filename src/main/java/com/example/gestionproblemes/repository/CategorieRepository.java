package com.example.gestionproblemes.repository;

import com.example.gestionproblemes.model.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategorieRepository extends JpaRepository<Categorie, Long> {
    Optional<Categorie> findByNomIgnoreCase(String nom);
    boolean existsByNomIgnoreCase(String nom);
    List<Categorie> findByActiveTrue();
}
