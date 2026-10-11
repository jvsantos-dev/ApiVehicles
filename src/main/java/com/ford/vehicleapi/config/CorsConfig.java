package com.ford.vehicleapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS: libera o acesso pelo navegador apenas para as origens configuradas em {@code app.cors.allowed-origins}.
 * <p>
 * Aplicado pelo Spring Security ({@code http.cors(...)}), antes da autenticação, para que o preflight
 * (OPTIONS) seja respondido sem exigir token.
 */
@Configuration
public class CorsConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(properties.allowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT));
        // Sem isso o JavaScript do front-end não consegue ler o Location do 201 nem o WWW-Authenticate do 401.
        config.setExposedHeaders(List.of(HttpHeaders.LOCATION, HttpHeaders.WWW_AUTHENTICATE));
        // Autenticação é por header Bearer, não por cookie.
        config.setAllowCredentials(false);
        config.setMaxAge(properties.maxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
