package com.example.gestionproblemes.config;

import com.example.gestionproblemes.enums.*;
import com.example.gestionproblemes.model.*;
import com.example.gestionproblemes.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Optional, entirely fictional portfolio data; never run against a real database. */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.data.initialize", havingValue = "true", matchIfMissing = false)
public class DataInitializer implements CommandLineRunner {
    private final DepartementRepository departementRepository;
    private final CategorieRepository categorieRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.demo.password}")
    private String demoPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (utilisateurRepository.count() > 0) return;
        if (demoPassword == null || demoPassword.length() < 12) {
            throw new IllegalArgumentException("DEMO_PASSWORD must contain at least 12 characters.");
        }
        Departement department = departementRepository.save(Departement.builder()
                .nom("Demo IT Department").description("Fictional portfolio department").build());
        categorieRepository.save(Categorie.builder().nom("Demo hardware")
                .description("Fictional equipment requests").delaiCibleHeures(24).active(true).build());
        create(Administrateur.builder().niveauAcces(3).build(), "admin", Role.ADMINISTRATEUR, department);
        create(ResponsableSupport.builder().service("Demo support").niveauSupervision(2).build(),
                "support", Role.RESPONSABLE_SUPPORT, department);
        create(Technicien.builder().specialite("Demo hardware").niveauExpertise(1)
                .chargeTravail(0).disponible(true).build(), "technician", Role.TECHNICIEN, department);
        create(Employe.builder().poste("Demo employee").build(), "employee", Role.EMPLOYE, department);
    }

    private void create(Utilisateur user, String name, Role role, Departement department) {
        user.setNom("Demo");
        user.setPrenom(name);
        user.setEmail(name + "@example.com");
        user.setMotDePasse(passwordEncoder.encode(demoPassword));
        user.setRole(role);
        user.setStatutCompte(StatutCompte.ACTIF);
        user.setDepartement(department);
        utilisateurRepository.save(user);
    }
}
