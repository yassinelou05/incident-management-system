package com.example.gestionproblemes.security;

import com.example.gestionproblemes.model.Utilisateur;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/** Acces a l'utilisateur authentifie depuis le contexte de securite. */
public final class UtilisateurConnecte {

    private UtilisateurConnecte() {
    }

    public static Utilisateur get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UtilisateurPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Aucun utilisateur authentifie.");
        }
        return principal.getUtilisateur();
    }
}
