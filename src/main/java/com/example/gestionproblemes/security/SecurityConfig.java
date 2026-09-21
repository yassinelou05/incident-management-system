package com.example.gestionproblemes.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration de la securite : authentification par jeton JWT, sessions sans
 * etat, autorisations par role et politique CORS.
 *
 * L'authentification est realisee directement par AdministrateurService
 * (comparaison BCrypt puis emission du jeton). Aucun AuthenticationManager ni
 * AuthenticationProvider n'est donc declare ici : la configuration reste
 * compatible avec Spring Security 6 comme avec Spring Security 7.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${application.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // --------- Acces public ---------
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/sante").permitAll()
                .requestMatchers("/error").permitAll()

                // --------- Administration ---------
                .requestMatchers("/api/utilisateurs/**").hasRole("ADMINISTRATEUR")
                .requestMatchers(HttpMethod.POST, "/api/categories/**", "/api/departements/**")
                    .hasRole("ADMINISTRATEUR")
                .requestMatchers(HttpMethod.PUT, "/api/categories/**", "/api/departements/**")
                    .hasRole("ADMINISTRATEUR")
                .requestMatchers(HttpMethod.DELETE, "/api/categories/**", "/api/departements/**")
                    .hasRole("ADMINISTRATEUR")

                // --------- Supervision ---------
                .requestMatchers("/api/problemes/*/affecter", "/api/problemes/*/reaffecter",
                                 "/api/problemes/*/priorite", "/api/problemes/*/cloturer")
                    .hasAnyRole("RESPONSABLE_SUPPORT", "ADMINISTRATEUR")
                .requestMatchers("/api/tableau-bord/statistiques")
                    .hasAnyRole("RESPONSABLE_SUPPORT", "ADMINISTRATEUR")

                // --------- Traitement technique ---------
                .requestMatchers("/api/problemes/*/diagnostic", "/api/problemes/*/resoudre",
                                 "/api/problemes/*/prendre-en-charge")
                    .hasAnyRole("TECHNICIEN", "RESPONSABLE_SUPPORT", "ADMINISTRATEUR")

                // --------- Declaration : ouverte a tous les roles ---------
                .requestMatchers(HttpMethod.POST, "/api/problemes")
                    .hasAnyRole("EMPLOYE", "TECHNICIEN", "RESPONSABLE_SUPPORT", "ADMINISTRATEUR")

                // --------- Toute autre requete : authentification obligatoire ---------
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
