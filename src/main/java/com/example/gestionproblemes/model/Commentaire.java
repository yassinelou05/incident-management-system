package com.example.gestionproblemes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

/** Commentaire attache a un probleme : supprime avec le probleme (composition). */
@Entity
@Table(name = "commentaire")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commentaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_commentaire")
    private Long idCommentaire;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    /** Commentaire interne : visible uniquement par le support et l'administration. */
    private Boolean interne;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_probleme", nullable = false,
                foreignKey = @ForeignKey(name = "fk_commentaire_probleme"))
    private Probleme probleme;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_auteur", nullable = false,
                foreignKey = @ForeignKey(name = "fk_commentaire_auteur"))
    private Utilisateur auteur;

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (interne == null) {
            interne = Boolean.FALSE;
        }
    }

    public void modifier(String nouveauContenu) {
        this.contenu = nouveauContenu;
        this.dateModification = LocalDateTime.now();
    }
}
