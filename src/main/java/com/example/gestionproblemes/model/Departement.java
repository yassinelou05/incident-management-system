package com.example.gestionproblemes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/** Entite organisationnelle regroupant plusieurs utilisateurs. */
@Entity
@Table(name = "departement",
       uniqueConstraints = @UniqueConstraint(name = "uk_departement_nom", columnNames = "nom"))
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Departement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_departement")
    private Long idDepartement;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String nom;

    @Column(length = 500)
    private String description;

    @Column(length = 150)
    private String localisation;

    @JsonIgnore
    @OneToMany(mappedBy = "departement", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Utilisateur> utilisateurs = new ArrayList<>();

    @JsonIgnore
    public int getNombreUtilisateurs() {
        return utilisateurs == null ? 0 : utilisateurs.size();
    }
}
