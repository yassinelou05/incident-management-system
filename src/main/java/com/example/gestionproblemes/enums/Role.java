package com.example.gestionproblemes.enums;

/** Roles applicatifs determinant les autorisations. */
public enum Role {
    EMPLOYE,
    TECHNICIEN,
    RESPONSABLE_SUPPORT,
    ADMINISTRATEUR;

    /** Autorite Spring Security associee au role. */
    public String authority() {
        return "ROLE_" + name();
    }
}
