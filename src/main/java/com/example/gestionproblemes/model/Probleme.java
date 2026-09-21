package com.example.gestionproblemes.model;

import com.example.gestionproblemes.enums.Priorite;
import com.example.gestionproblemes.enums.StatutProbleme;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Entite centrale du domaine : porte le cycle de vie complet d'une declaration. */
@Entity
@Table(name = "probleme",
       uniqueConstraints = @UniqueConstraint(name = "uk_probleme_reference", columnNames = "reference"),
       indexes = {
           @Index(name = "idx_probleme_statut", columnList = "statut"),
           @Index(name = "idx_probleme_priorite", columnList = "priorite")
       })
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Probleme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_probleme")
    private Long idProbleme;

    /** Reference lisible generee a la creation : PRB-2026-000123. */
    @Column(nullable = false, length = 30)
    private String reference;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String titre;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "equipement_concerne", length = 200)
    private String equipementConcerne;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutProbleme statut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priorite priorite;

    @Column(columnDefinition = "TEXT")
    private String diagnostic;

    @Column(columnDefinition = "TEXT")
    private String solution;

    @Column(name = "date_declaration", nullable = false, updatable = false)
    private LocalDateTime dateDeclaration;

    @Column(name = "date_affectation")
    private LocalDateTime dateAffectation;

    @Column(name = "date_prise_en_charge")
    private LocalDateTime datePriseEnCharge;

    @Column(name = "date_resolution")
    private LocalDateTime dateResolution;

    @Column(name = "date_cloture")
    private LocalDateTime dateCloture;

    @Column(name = "resolution_confirmee")
    private Boolean resolutionConfirmee;

    /**
     * Auteur de la declaration. Type Utilisateur et non Employe : un technicien
     * peut lui aussi signaler un probleme qu'il constate sur le parc.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_declarant", nullable = false,
                foreignKey = @ForeignKey(name = "fk_probleme_declarant"))
    private Utilisateur declarant;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_categorie", nullable = false,
                foreignKey = @ForeignKey(name = "fk_probleme_categorie"))
    private Categorie categorie;

    @OneToMany(mappedBy = "probleme", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Affectation> affectations = new ArrayList<>();

    @OneToMany(mappedBy = "probleme", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Commentaire> commentaires = new ArrayList<>();

    @OneToMany(mappedBy = "probleme", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Historique> historiques = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (dateDeclaration == null) {
            dateDeclaration = LocalDateTime.now();
        }
        if (statut == null) {
            statut = StatutProbleme.NOUVEAU;
        }
        if (resolutionConfirmee == null) {
            resolutionConfirmee = Boolean.FALSE;
        }
    }

    /** Affectation active courante, s'il en existe une. */
    @JsonIgnore
    public Affectation getAffectationActive() {
        if (affectations == null) {
            return null;
        }
        return affectations.stream()
                .filter(a -> Boolean.TRUE.equals(a.getActive()))
                .findFirst()
                .orElse(null);
    }

    /** Technicien actuellement en charge, expose directement dans le JSON. */
    public Technicien getTechnicienAssigne() {
        Affectation active = getAffectationActive();
        return active == null ? null : active.getTechnicien();
    }

    /** Un probleme cloture n'est plus modifiable. */
    public boolean isModifiable() {
        return statut != StatutProbleme.FERME;
    }

    @JsonIgnore
    public Duration calculerDelaiResolution() {
        if (dateDeclaration == null || dateResolution == null) {
            return null;
        }
        return Duration.between(dateDeclaration, dateResolution);
    }

    public Long getDelaiResolutionHeures() {
        Duration d = calculerDelaiResolution();
        return d == null ? null : d.toHours();
    }
}
