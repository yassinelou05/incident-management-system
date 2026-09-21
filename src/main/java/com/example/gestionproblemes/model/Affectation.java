package com.example.gestionproblemes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Reification de la relation Probleme - Technicien : conserve la trace des
 * affectations successives et des reaffectations.
 */
@Entity
@Table(name = "affectation")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Affectation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_affectation")
    private Long idAffectation;

    @Column(name = "date_affectation", nullable = false)
    private LocalDateTime dateAffectation;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @Column(name = "note_affectation", length = 1000)
    private String noteAffectation;

    @Column(nullable = false)
    private Boolean active;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_probleme", nullable = false,
                foreignKey = @ForeignKey(name = "fk_affectation_probleme"))
    private Probleme probleme;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_technicien", nullable = false,
                foreignKey = @ForeignKey(name = "fk_affectation_technicien"))
    private Technicien technicien;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_affecteur", foreignKey = @ForeignKey(name = "fk_affectation_affecteur"))
    private Utilisateur affectePar;

    @PrePersist
    protected void onCreate() {
        if (dateAffectation == null) {
            dateAffectation = LocalDateTime.now();
        }
        if (active == null) {
            active = Boolean.TRUE;
        }
    }

    /** Cloture l'affectation courante lors d'une reaffectation. */
    public void cloturerAffectation() {
        this.active = Boolean.FALSE;
        this.dateFin = LocalDateTime.now();
    }
}
