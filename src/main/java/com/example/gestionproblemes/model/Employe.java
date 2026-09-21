package com.example.gestionproblemes.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "employe")
@PrimaryKeyJoinColumn(name = "id_utilisateur")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Employe extends Utilisateur {

    @Column(length = 100)
    private String poste;

    @Column(length = 60)
    private String bureau;

    @Column(name = "poste_telephonique", length = 20)
    private String posteTelephonique;

    @Override
    public List<String> getPermissions() {
        return List.of(
                "PROBLEME_DECLARER",
                "PROBLEME_LIRE_PROPRES",
                "COMMENTAIRE_AJOUTER",
                "RESOLUTION_CONFIRMER"
        );
    }
}
