package com.example.gestionproblemes.service;

import com.example.gestionproblemes.enums.Role;
import com.example.gestionproblemes.enums.TypeNotification;
import com.example.gestionproblemes.model.Notification;
import com.example.gestionproblemes.model.Probleme;
import com.example.gestionproblemes.model.Utilisateur;
import com.example.gestionproblemes.repository.NotificationRepository;
import com.example.gestionproblemes.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** Envoi et consultation des notifications applicatives. */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Transactional
    public void notifier(Utilisateur destinataire, TypeNotification type, String message, Probleme probleme) {
        if (destinataire == null) {
            return;
        }
        notificationRepository.save(Notification.builder()
                .destinataire(destinataire)
                .typeNotification(type)
                .message(message)
                .probleme(probleme)
                .lue(Boolean.FALSE)
                .build());
    }

    /** Diffuse une notification a tous les responsables support. */
    @Transactional
    public void notifierSuperviseurs(TypeNotification type, String message, Probleme probleme) {
        List<Utilisateur> destinataires = utilisateurRepository.findByRole(Role.RESPONSABLE_SUPPORT);
        destinataires.forEach(u -> notifier(u, type, message, probleme));
    }

    @Transactional(readOnly = true)
    public List<Notification> mesNotifications(Long idUtilisateur) {
        return notificationRepository.findByDestinataireIdUtilisateurOrderByDateEnvoiDesc(idUtilisateur);
    }

    @Transactional(readOnly = true)
    public long compterNonLues(Long idUtilisateur) {
        return notificationRepository.countByDestinataireIdUtilisateurAndLueFalse(idUtilisateur);
    }

    @Transactional
    public Notification marquerCommeLue(Long idNotification, Long idUtilisateur) {
        Notification notification = notificationRepository.findById(idNotification)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification introuvable."));
        if (!notification.getDestinataire().getIdUtilisateur().equals(idUtilisateur)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cette notification ne vous appartient pas.");
        }
        notification.marquerCommeLue();
        return notificationRepository.save(notification);
    }

    @Transactional
    public int marquerToutesCommeLues(Long idUtilisateur) {
        return notificationRepository.marquerToutesCommeLues(idUtilisateur);
    }
}
