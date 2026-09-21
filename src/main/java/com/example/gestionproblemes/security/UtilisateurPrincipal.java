package com.example.gestionproblemes.security;

import com.example.gestionproblemes.enums.StatutCompte;
import com.example.gestionproblemes.model.Utilisateur;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Adaptateur entre l'entite Utilisateur et le modele de securite de Spring. */
@Getter
@RequiredArgsConstructor
public class UtilisateurPrincipal implements UserDetails {

    private final Utilisateur utilisateur;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(utilisateur.getRole().authority()));
    }

    @Override
    public String getPassword() {
        return utilisateur.getMotDePasse();
    }

    @Override
    public String getUsername() {
        return utilisateur.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return utilisateur.getStatutCompte() != StatutCompte.SUSPENDU;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return utilisateur.getStatutCompte() == StatutCompte.ACTIF;
    }

    public Long getIdUtilisateur() {
        return utilisateur.getIdUtilisateur();
    }
}
