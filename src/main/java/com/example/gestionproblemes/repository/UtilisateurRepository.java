package com.example.gestionproblemes.repository;

import com.example.gestionproblemes.enums.Role;
import com.example.gestionproblemes.enums.StatutCompte;
import com.example.gestionproblemes.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long>,
                                               JpaSpecificationExecutor<Utilisateur> {

    Optional<Utilisateur> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<Utilisateur> findByRole(Role role);

    List<Utilisateur> findByStatutCompte(StatutCompte statutCompte);

    List<Utilisateur> findByDepartementIdDepartement(Long idDepartement);

    long countByRole(Role role);
}
