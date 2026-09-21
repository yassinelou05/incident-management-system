package com.example.gestionproblemes.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;


@Entity
@Table(name = "administrateur")
@PrimaryKeyJoinColumn(name = "id_utilisateur")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Administrateur extends Utilisateur {

    @Column(name = "niveau_acces")
    private Integer niveauAcces;

    @Override
    public List<String> getPermissions() {
        return List.of(
                "UTILISATEUR_GERER",
                "ROLE_ATTRIBUER",
                "COMPTE_ACTIVER",
                "CATEGORIE_GERER",
                "DEPARTEMENT_GERER",
                "HISTORIQUE_CONSULTER",
                "PROBLEME_LIRE_TOUS",
                "PROBLEME_AFFECTER"
        );
    }
}
