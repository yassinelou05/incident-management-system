package com.example.gestionproblemes.model;

import com.example.gestionproblemes.enums.Role;
import com.example.gestionproblemes.enums.StatutCompte;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "utilisateur",
       uniqueConstraints = @UniqueConstraint(name = "uk_utilisateur_email", columnNames = "email"))
@Inheritance(strategy = InheritanceType.JOINED)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Long idUtilisateur;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String nom;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String prenom;

    @Email
    @NotBlank
    @Column(nullable = false, length = 150)
    private String email;

    /** Jamais renvoye dans les reponses JSON, mais acceptee en entree. */
    @NotBlank
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "mot_de_passe", nullable = false, length = 120)
    private String motDePasse;

    @Column(length = 30)
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_compte", nullable = false, length = 20)
    private StatutCompte statutCompte;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_departement", foreignKey = @ForeignKey(name = "fk_utilisateur_departement"))
    private Departement departement;

    /** Problemes declares par cet utilisateur, quel que soit son role. */
    @JsonIgnore
    @OneToMany(mappedBy = "declarant", fetch = FetchType.LAZY)
    private List<Probleme> problemesDeclares = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "auteur", fetch = FetchType.LAZY)
    private List<Commentaire> commentaires = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "destinataire", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Notification> notifications = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (statutCompte == null) {
            statutCompte = StatutCompte.ACTIF;
        }
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    @JsonIgnore
    public boolean estActif() {
        return statutCompte == StatutCompte.ACTIF;
    }

    /** Permissions fonctionnelles propres a chaque role. */
    public abstract List<String> getPermissions();
}
