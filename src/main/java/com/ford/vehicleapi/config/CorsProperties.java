package com.ford.vehicleapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Configuração de CORS (prefixo {@code app.cors}).
 *
 * @param allowedOrigins origens (front-ends) autorizadas a chamar a API pelo navegador.
 * @param maxAge         por quanto tempo o navegador pode reaproveitar a resposta do preflight.
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins, Duration maxAge) {
}
