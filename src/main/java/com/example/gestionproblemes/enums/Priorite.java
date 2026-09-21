package com.example.gestionproblemes.enums;

/** Niveau de priorite avec le delai cible de prise en charge, en heures. */
public enum Priorite {

    BASSE(72),
    MOYENNE(48),
    HAUTE(24),
    CRITIQUE(4);

    private final int delaiCibleHeures;

    Priorite(int delaiCibleHeures) {
        this.delaiCibleHeures = delaiCibleHeures;
    }

    public int getDelaiCibleHeures() {
        return delaiCibleHeures;
    }
}
