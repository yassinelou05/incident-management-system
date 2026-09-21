package com.example.gestionproblemes.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * Cycle de vie d'un probleme technique.
 *
 * Chaque statut porte un rang qui traduit sa position dans la progression :
 *
 *   1. NOUVEAU     declaration enregistree, en attente d'analyse
 *   2. ASSIGNE     un technicien a ete designe
 *   3. EN_COURS    intervention en cours
 *   4. EN_ATTENTE  intervention suspendue (piece, information, tiers)
 *   5. RESOLU      une solution a ete appliquee
 *   6. FERME       traitement definitivement termine
 *
 * Regle generale : le statut ne peut qu'avancer dans cette progression.
 * Une seule exception, la mise en attente : EN_COURS et EN_ATTENTE forment un
 * couple suspendre / reprendre a l'interieur de la phase de traitement.
 *
 * Un probleme RESOLU ne peut donc plus revenir en traitement par un simple
 * changement de statut : seule la cloture reste possible. La reouverture
 * existe, mais elle passe par une operation dediee (refus de la resolution
 * par le declarant), qui trace explicitement l'action dans l'historique.
 */
public enum StatutProbleme {

    NOUVEAU(1),
    ASSIGNE(2),
    EN_COURS(3),
    EN_ATTENTE(4),
    RESOLU(5),
    FERME(6);

    private final int ordre;

    StatutProbleme(int ordre) {
        this.ordre = ordre;
    }

    public int getOrdre() {
        return ordre;
    }

    /** Statuts de la phase de traitement. */
    public static Set<StatutProbleme> phaseTraitement() {
        return EnumSet.of(ASSIGNE, EN_COURS, EN_ATTENTE);
    }

    public boolean estEnTraitement() {
        return phaseTraitement().contains(this);
    }

    /** Statuts atteignables depuis le statut courant. */
    public Set<StatutProbleme> transitionsAutorisees() {
        switch (this) {
            case NOUVEAU:
                return EnumSet.of(ASSIGNE, FERME);
            case ASSIGNE:
                return EnumSet.of(EN_COURS, EN_ATTENTE, RESOLU, FERME);
            case EN_COURS:
                // EN_ATTENTE : suspension du traitement.
                return EnumSet.of(EN_ATTENTE, RESOLU, FERME);
            case EN_ATTENTE:
                // EN_COURS : reprise du traitement, seul retour en arriere admis.
                return EnumSet.of(EN_COURS, RESOLU, FERME);
            case RESOLU:
                return EnumSet.of(FERME);
            default:
                return EnumSet.noneOf(StatutProbleme.class);
        }
    }

    public boolean peutPasserA(StatutProbleme cible) {
        return transitionsAutorisees().contains(cible);
    }

    /** Un probleme ferme n'est plus modifiable. */
    public boolean estTerminal() {
        return this == FERME;
    }

    /** Vrai si la cible fait revenir le probleme en arriere dans la progression. */
    public boolean estUnRetourEnArriere(StatutProbleme cible) {
        return cible != null && cible.ordre < this.ordre;
    }
}
