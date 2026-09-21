package com.example.gestionproblemes.repository;

import com.example.gestionproblemes.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByDestinataireIdUtilisateurOrderByDateEnvoiDesc(Long idDestinataire);

    long countByDestinataireIdUtilisateurAndLueFalse(Long idDestinataire);

    @Modifying
    @Query("UPDATE Notification n SET n.lue = true WHERE n.destinataire.idUtilisateur = :idUtilisateur")
    int marquerToutesCommeLues(@Param("idUtilisateur") Long idUtilisateur);
}
