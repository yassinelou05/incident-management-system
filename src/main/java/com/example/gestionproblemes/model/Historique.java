package com.example.gestionproblemes.model;

import com.example.gestionproblemes.enums.StatutProbleme;
import com.example.gestionproblemes.enums.TypeAction;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "historique",
       indexes = @Index(name = "idx_historique_probleme", columnList = "id_probleme"))
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Historique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique")
    private Long idHistorique;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_action", nullable = false, length = 30)
    private TypeAction typeAction;

    @Enumerated(EnumType.STRING)
    @Column(name = "ancien_statut", length = 20)
    private StatutProbleme ancienStatut;

    @Enumerated(EnumType.STRING)
    @Column(name = "nouveau_statut", length = 20)
    private StatutProbleme nouveauStatut;

    @Column(length = 1000)
    private String description;

    @Column(name = "date_action", nullable = false, updatable = false)
    private LocalDateTime dateAction;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_probleme", nullable = false,
                foreignKey = @ForeignKey(name = "fk_historique_probleme"))
    private Probleme probleme;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_auteur", foreignKey = @ForeignKey(name = "fk_historique_auteur"))
    private Utilisateur auteur;

    @PrePersist
    protected void onCreate() {
        if (dateAction == null) {
            dateAction = LocalDateTime.now();
        }
    }

    /** Libelle lisible utilise par le frontend dans la chronologie. */
    public String getLibelle() {
        if (ancienStatut != null && nouveauStatut != null) {
            return typeAction + " : " + ancienStatut + " -> " + nouveauStatut;
        }
        if (nouveauStatut != null) {
            return typeAction + " : " + nouveauStatut;
        }
        return typeAction == null ? "" : typeAction.name();
    }
}
