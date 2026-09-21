package com.example.gestionproblemes.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "responsable_support")
@PrimaryKeyJoinColumn(name = "id_utilisateur")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ResponsableSupport extends Utilisateur {

    @Column(length = 100)
    private String service;

    @Column(name = "niveau_supervision")
    private Integer niveauSupervision;

    @Override
    public List<String> getPermissions() {
        return List.of(
                "PROBLEME_LIRE_TOUS",
                "PROBLEME_AFFECTER",
                "PROBLEME_REAFFECTER",
                "PRIORITE_DEFINIR",
                "PROBLEME_CLOTURER",
                "STATISTIQUES_CONSULTER",
                "COMMENTAIRE_AJOUTER"
        );
    }
}
