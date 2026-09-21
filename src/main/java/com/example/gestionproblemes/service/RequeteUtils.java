package com.example.gestionproblemes.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;


public final class RequeteUtils {

    private RequeteUtils() {
    }

    public static String texte(Map<String, Object> corps, String cle) {
        Object valeur = corps == null ? null : corps.get(cle);
        return valeur == null ? null : String.valueOf(valeur).trim();
    }

    /** Champ obligatoire : leve une 400 si absent ou vide. */
    public static String texteObligatoire(Map<String, Object> corps, String cle, String libelle) {
        String valeur = texte(corps, cle);
        if (valeur == null || valeur.isBlank() || "null".equals(valeur)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le champ « " + libelle + " » est obligatoire.");
        }
        return valeur;
    }

    public static Long identifiant(Map<String, Object> corps, String cle) {
        String valeur = texte(corps, cle);
        if (valeur == null || valeur.isBlank() || "null".equals(valeur)) {
            return null;
        }
        try {
            return Long.valueOf(valeur);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identifiant invalide pour « " + cle + " ».");
        }
    }

    public static Long identifiantObligatoire(Map<String, Object> corps, String cle, String libelle) {
        Long valeur = identifiant(corps, cle);
        if (valeur == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le champ « " + libelle + " » est obligatoire.");
        }
        return valeur;
    }

    public static Integer entier(Map<String, Object> corps, String cle, Integer defaut) {
        String valeur = texte(corps, cle);
        if (valeur == null || valeur.isBlank() || "null".equals(valeur)) {
            return defaut;
        }
        try {
            return Integer.valueOf(valeur);
        } catch (NumberFormatException e) {
            return defaut;
        }
    }

    public static Boolean booleen(Map<String, Object> corps, String cle, Boolean defaut) {
        Object valeur = corps == null ? null : corps.get(cle);
        if (valeur == null) {
            return defaut;
        }
        if (valeur instanceof Boolean b) {
            return b;
        }
        return Boolean.valueOf(String.valueOf(valeur));
    }

    public static <E extends Enum<E>> E enumeration(Map<String, Object> corps, String cle,
                                                    Class<E> type, String libelle, boolean obligatoire) {
        String valeur = texte(corps, cle);
        if (valeur == null || valeur.isBlank() || "null".equals(valeur)) {
            if (obligatoire) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Le champ « " + libelle + " » est obligatoire.");
            }
            return null;
        }
        try {
            return Enum.valueOf(type, valeur.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Valeur invalide pour « " + libelle + " » : " + valeur);
        }
    }
}
