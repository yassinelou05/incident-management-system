package com.example.gestionproblemes.service;

import com.example.gestionproblemes.enums.Role;
import com.example.gestionproblemes.enums.StatutCompte;
import com.example.gestionproblemes.model.*;
import com.example.gestionproblemes.repository.DepartementRepository;
import com.example.gestionproblemes.repository.UtilisateurRepository;
import com.example.gestionproblemes.repository.UtilisateurSpecifications;
import com.example.gestionproblemes.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class AdministrateurService {

    @org.springframework.beans.factory.annotation.Value("${application.accounts.temporary-password}")
    private String motDePasseTemporaire;

    private final UtilisateurRepository utilisateurRepository;
    private final DepartementRepository departementRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;



    @Transactional
    public Map<String, Object> authentifier(Map<String, Object> corps) {
        String email = RequeteUtils.texteObligatoire(corps, "email", "email");
        String motDePasse = RequeteUtils.texteObligatoire(corps, "motDePasse", "mot de passe");

        Utilisateur utilisateur = utilisateurRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides."));

        if (!passwordEncoder.matches(motDePasse, utilisateur.getMotDePasse())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides.");
        }
        if (utilisateur.getStatutCompte() != StatutCompte.ACTIF) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Votre compte est " + utilisateur.getStatutCompte().name().toLowerCase()
                    + ". Contactez l'administrateur.");
        }

        utilisateur.setDerniereConnexion(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);

        log.info("Connexion reussie : {} ({})", utilisateur.getEmail(), utilisateur.getRole());

        Map<String, Object> reponse = new LinkedHashMap<>();
        reponse.put("token", jwtService.genererToken(utilisateur));
        reponse.put("typeToken", "Bearer");
        reponse.put("expiration", jwtService.getExpiration());
        reponse.put("idUtilisateur", utilisateur.getIdUtilisateur());
        reponse.put("nomComplet", utilisateur.getNomComplet());
        reponse.put("email", utilisateur.getEmail());
        reponse.put("role", utilisateur.getRole());
        reponse.put("departement", utilisateur.getDepartement() == null
                ? null : utilisateur.getDepartement().getNom());
        reponse.put("permissions", utilisateur.getPermissions());
        return reponse;
    }

    @Transactional
    public void changerMotDePasse(Utilisateur utilisateur, Map<String, Object> corps) {
        String ancien = RequeteUtils.texteObligatoire(corps, "ancienMotDePasse", "ancien mot de passe");
        String nouveau = RequeteUtils.texteObligatoire(corps, "nouveauMotDePasse", "nouveau mot de passe");

        if (!passwordEncoder.matches(ancien, utilisateur.getMotDePasse())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'ancien mot de passe est incorrect.");
        }
        if (nouveau.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le mot de passe doit contenir au moins 8 caracteres.");
        }
        if (passwordEncoder.matches(nouveau, utilisateur.getMotDePasse())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le nouveau mot de passe doit etre different de l'ancien.");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(nouveau));
        utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void reinitialiserMotDePasse(Long idUtilisateur) {
        Utilisateur utilisateur = chargerUtilisateur(idUtilisateur);
        utilisateur.setMotDePasse(passwordEncoder.encode(motDePasseTemporaire));
        utilisateurRepository.save(utilisateur);
        log.info("Mot de passe reinitialise pour {}", utilisateur.getEmail());
    }


    @Transactional(readOnly = true)
    public List<Utilisateur> rechercher(Role role, StatutCompte statut, Long idDepartement, String motCle) {
        return utilisateurRepository.findAll(
                UtilisateurSpecifications.filtrer(role, statut, idDepartement, motCle));
    }

    @Transactional(readOnly = true)
    public Utilisateur chargerUtilisateur(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Utilisateur introuvable (identifiant : " + id + ")."));
    }

    @Transactional
    public Utilisateur creer(Map<String, Object> corps) {
        String email = RequeteUtils.texteObligatoire(corps, "email", "email").toLowerCase();
        if (utilisateurRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un compte existe deja avec l'email " + email + ".");
        }

        Role role = RequeteUtils.enumeration(corps, "role", Role.class, "role", true);
        String motDePasse = RequeteUtils.texte(corps, "motDePasse");

        Utilisateur utilisateur;
        switch (role) {
            case EMPLOYE -> utilisateur = Employe.builder()
                    .poste(RequeteUtils.texte(corps, "poste"))
                    .bureau(RequeteUtils.texte(corps, "bureau"))
                    .posteTelephonique(RequeteUtils.texte(corps, "posteTelephonique"))
                    .build();
            case TECHNICIEN -> utilisateur = Technicien.builder()
                    .specialite(RequeteUtils.texte(corps, "specialite"))
                    .niveauExpertise(RequeteUtils.entier(corps, "niveauExpertise", 1))
                    .chargeTravail(0)
                    .disponible(Boolean.TRUE)
                    .build();
            case RESPONSABLE_SUPPORT -> utilisateur = ResponsableSupport.builder()
                    .service(RequeteUtils.texte(corps, "service"))
                    .niveauSupervision(RequeteUtils.entier(corps, "niveauSupervision", 1))
                    .build();
            default -> utilisateur = Administrateur.builder()
                    .niveauAcces(RequeteUtils.entier(corps, "niveauAcces", 1))
                    .build();
        }

        utilisateur.setNom(RequeteUtils.texteObligatoire(corps, "nom", "nom"));
        utilisateur.setPrenom(RequeteUtils.texteObligatoire(corps, "prenom", "prenom"));
        utilisateur.setEmail(email);
        utilisateur.setMotDePasse(passwordEncoder.encode(
                motDePasse == null || motDePasse.isBlank() ? motDePasseTemporaire : motDePasse));
        utilisateur.setTelephone(RequeteUtils.texte(corps, "telephone"));
        utilisateur.setRole(role);
        utilisateur.setStatutCompte(StatutCompte.ACTIF);
        utilisateur.setDepartement(resoudreDepartement(RequeteUtils.identifiant(corps, "idDepartement")));

        log.info("Creation du compte {} ({})", email, role);
        return utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public Utilisateur modifier(Long id, Map<String, Object> corps) {
        Utilisateur utilisateur = chargerUtilisateur(id);
        String email = RequeteUtils.texteObligatoire(corps, "email", "email").toLowerCase();

        if (!utilisateur.getEmail().equalsIgnoreCase(email)
                && utilisateurRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un compte existe deja avec l'email " + email + ".");
        }

        Role role = RequeteUtils.enumeration(corps, "role", Role.class, "role", false);
        if (role != null && role != utilisateur.getRole()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le role d'un compte existant ne peut pas etre modifie.");
        }

        utilisateur.setNom(RequeteUtils.texteObligatoire(corps, "nom", "nom"));
        utilisateur.setPrenom(RequeteUtils.texteObligatoire(corps, "prenom", "prenom"));
        utilisateur.setEmail(email);
        utilisateur.setTelephone(RequeteUtils.texte(corps, "telephone"));
        utilisateur.setDepartement(resoudreDepartement(RequeteUtils.identifiant(corps, "idDepartement")));

        String motDePasse = RequeteUtils.texte(corps, "motDePasse");
        if (motDePasse != null && !motDePasse.isBlank()) {
            utilisateur.setMotDePasse(passwordEncoder.encode(motDePasse));
        }

        if (utilisateur instanceof Employe e) {
            e.setPoste(RequeteUtils.texte(corps, "poste"));
            e.setBureau(RequeteUtils.texte(corps, "bureau"));
            e.setPosteTelephonique(RequeteUtils.texte(corps, "posteTelephonique"));
        } else if (utilisateur instanceof Technicien t) {
            t.setSpecialite(RequeteUtils.texte(corps, "specialite"));
            t.setNiveauExpertise(RequeteUtils.entier(corps, "niveauExpertise", t.getNiveauExpertise()));
        } else if (utilisateur instanceof ResponsableSupport r) {
            r.setService(RequeteUtils.texte(corps, "service"));
            r.setNiveauSupervision(RequeteUtils.entier(corps, "niveauSupervision", r.getNiveauSupervision()));
        } else if (utilisateur instanceof Administrateur a) {
            a.setNiveauAcces(RequeteUtils.entier(corps, "niveauAcces", a.getNiveauAcces()));
        }

        return utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public Utilisateur changerStatutCompte(Long id, StatutCompte statut) {
        Utilisateur utilisateur = chargerUtilisateur(id);
        utilisateur.setStatutCompte(statut);
        return utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public Utilisateur changerDisponibilite(Long id, boolean disponible) {
        Utilisateur utilisateur = chargerUtilisateur(id);
        if (!(utilisateur instanceof Technicien technicien)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La disponibilite ne s'applique qu'aux techniciens.");
        }
        technicien.setDisponible(disponible);
        return utilisateurRepository.save(technicien);
    }

    @Transactional(readOnly = true)
    public List<Utilisateur> techniciens() {
        return utilisateurRepository.findByRole(Role.TECHNICIEN).stream()
                .filter(u -> u.getStatutCompte() == StatutCompte.ACTIF)
                .toList();
    }

    private Departement resoudreDepartement(Long idDepartement) {
        if (idDepartement == null) {
            return null;
        }
        return departementRepository.findById(idDepartement)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Departement introuvable."));
    }
}
