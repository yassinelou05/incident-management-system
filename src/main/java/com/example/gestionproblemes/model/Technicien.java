package com.example.gestionproblemes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "technicien")
@PrimaryKeyJoinColumn(name = "id_utilisateur")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Technicien extends Utilisateur {

    @Column(length = 100)
    private String specialite;

    @Column(name = "niveau_expertise")
    private Integer niveauExpertise;

    @Column(name = "charge_travail")
    private Integer chargeTravail;

    private Boolean disponible;

    @JsonIgnore
    @OneToMany(mappedBy = "technicien", fetch = FetchType.LAZY)
    private List<Affectation> affectations = new ArrayList<>();

    public void incrementerCharge() {
        this.chargeTravail = (this.chargeTravail == null ? 0 : this.chargeTravail) + 1;
    }

    public void decrementerCharge() {
        int courante = this.chargeTravail == null ? 0 : this.chargeTravail;
        this.chargeTravail = Math.max(0, courante - 1);
    }

    @Override
    public List<String> getPermissions() {
        return List.of(
                "PROBLEME_DECLARER",
                "PROBLEME_LIRE_PROPRES",
                "PROBLEME_LIRE_ASSIGNES",
                "DIAGNOSTIC_SAISIR",
                "STATUT_CHANGER",
                "SOLUTION_APPLIQUER",
                "COMMENTAIRE_AJOUTER"
        );
    }
}
