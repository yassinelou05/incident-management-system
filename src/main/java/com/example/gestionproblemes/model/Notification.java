package com.example.gestionproblemes.model;

import com.example.gestionproblemes.enums.TypeNotification;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Message adresse a un utilisateur lors d'un evenement du workflow. */
@Entity
@Table(name = "notification",
       indexes = @Index(name = "idx_notification_destinataire", columnList = "id_destinataire"))
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notification")
    private Long idNotification;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_notification", nullable = false, length = 30)
    private TypeNotification typeNotification;

    @Column(nullable = false)
    private Boolean lue;

    @Column(name = "date_envoi", nullable = false, updatable = false)
    private LocalDateTime dateEnvoi;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_destinataire", nullable = false,
                foreignKey = @ForeignKey(name = "fk_notification_destinataire"))
    private Utilisateur destinataire;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_probleme", foreignKey = @ForeignKey(name = "fk_notification_probleme"))
    private Probleme probleme;

    @PrePersist
    protected void onCreate() {
        if (dateEnvoi == null) {
            dateEnvoi = LocalDateTime.now();
        }
        if (lue == null) {
            lue = Boolean.FALSE;
        }
    }

    /** Identifiant du probleme concerne, expose a plat pour le frontend. */
    public Long getIdProbleme() {
        return probleme == null ? null : probleme.getIdProbleme();
    }

    /** Reference du probleme concerne, exposee a plat pour le frontend. */
    public String getReferenceProbleme() {
        return probleme == null ? null : probleme.getReference();
    }

    public void marquerCommeLue() {
        this.lue = Boolean.TRUE;
    }
}
