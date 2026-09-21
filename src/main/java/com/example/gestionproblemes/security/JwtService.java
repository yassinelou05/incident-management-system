package com.example.gestionproblemes.security;

import com.example.gestionproblemes.model.Utilisateur;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/** Generation et validation des jetons JWT. */
@Service
public class JwtService {

    /** Signing key must be supplied through configuration. */
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration:86400000}")
    private long expiration;

    public String genererToken(Utilisateur utilisateur) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("idUtilisateur", utilisateur.getIdUtilisateur());
        claims.put("role", utilisateur.getRole().name());
        claims.put("nomComplet", utilisateur.getNomComplet());

        long maintenant = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(utilisateur.getEmail())
                .setIssuedAt(new Date(maintenant))
                .setExpiration(new Date(maintenant + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extraireEmail(String token) {
        return extraireClaim(token, Claims::getSubject);
    }

    public String extraireRole(String token) {
        return extraireClaim(token, claims -> claims.get("role", String.class));
    }

    public <T> T extraireClaim(String token, Function<Claims, T> resolveur) {
        return resolveur.apply(extraireTousLesClaims(token));
    }

    public boolean estValide(String token, String email) {
        return email.equalsIgnoreCase(extraireEmail(token)) && !estExpire(token);
    }

    public long getExpiration() {
        return expiration;
    }

    private boolean estExpire(String token) {
        return extraireClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extraireTousLesClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] cle = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(cle);
    }
}
