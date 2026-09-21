package com.example.gestionproblemes.repository;

import com.example.gestionproblemes.model.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long> {
    Optional<Employe> findByEmailIgnoreCase(String email);
}
