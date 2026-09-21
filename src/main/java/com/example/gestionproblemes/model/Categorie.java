package com.example.gestionproblemes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/** Categorie fonctionnelle d'un probleme : materiel, logiciel, reseau, acces, autre. */
@Entity
@Table(name = "categorie",
       uniqueConstraints = @UniqueConstraint(name = "uk_categorie_nom", columnNames = "nom"))
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categorie")
    private Long idCategorie;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String nom;

    @Column(length = 500)
    private String description;

    /** Delai cible de resolution en heures, utilise par les indicateurs. */
    @Column(name = "delai_cible_heures")
    private Integer delaiCibleHeures;

    private Boolean active;

    @JsonIgnore
    @OneToMany(mappedBy = "categorie", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Probleme> problemes = new ArrayList<>();

    public void activer() {
        this.active = Boolean.TRUE;
    }

    public void desactiver() {
        this.active = Boolean.FALSE;
    }
}
