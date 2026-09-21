package com.example.gestionproblemes.enums;

/** Nature d'une entree d'historique : toute modification importante est tracee. */
public enum TypeAction {
    CREATION,
    AFFECTATION,
    CHANGEMENT_STATUT,
    CHANGEMENT_PRIORITE,
    COMMENTAIRE,
    RESOLUTION,
    CLOTURE,
    REOUVERTURE
}
