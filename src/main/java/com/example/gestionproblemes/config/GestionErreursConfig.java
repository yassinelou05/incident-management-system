package com.example.gestionproblemes.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Gestion centralisee des erreurs : toutes les reponses suivent le meme format JSON. */
@Slf4j
@RestControllerAdvice
public class GestionErreursConfig {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex,
                                                                    HttpServletRequest requete) {
        HttpStatus statut = HttpStatus.valueOf(ex.getStatusCode().value());
        return ResponseEntity.status(statut)
                .body(corps(statut, ex.getReason(), requete, null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccesRefuse(AccessDeniedException ex,
                                                                  HttpServletRequest requete) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(corps(HttpStatus.FORBIDDEN,
                        "Vous n'avez pas l'autorisation d'effectuer cette action.", requete, null));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentification(AuthenticationException ex,
                                                                       HttpServletRequest requete) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(corps(HttpStatus.UNAUTHORIZED, "Identifiants invalides.", requete, null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex,
                                                                 HttpServletRequest requete) {
        Map<String, String> champs = new HashMap<>();
        for (FieldError erreur : ex.getBindingResult().getFieldErrors()) {
            champs.put(erreur.getField(), erreur.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(corps(HttpStatus.BAD_REQUEST, "Certains champs sont invalides.", requete, champs));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleInattendue(Exception ex, HttpServletRequest requete) {
        log.error("Erreur inattendue sur {} : {}", requete.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(corps(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Une erreur interne est survenue.", requete, null));
    }

    private Map<String, Object> corps(HttpStatus statut, String message,
                                      HttpServletRequest requete, Map<String, String> champs) {
        Map<String, Object> resultat = new LinkedHashMap<>();
        resultat.put("horodatage", LocalDateTime.now());
        resultat.put("statut", statut.value());
        resultat.put("erreur", statut.getReasonPhrase());
        resultat.put("message", message == null ? statut.getReasonPhrase() : message);
        resultat.put("chemin", requete.getRequestURI());
        if (champs != null && !champs.isEmpty()) {
            resultat.put("champs", champs);
        }
        return resultat;
    }
}
