package com.ford.vehicleapi.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuração do JWT (prefixo {@code app.jwt}).
 *
 * @param secret     chave HMAC-SHA256 (mínimo 32 bytes). Em produção, informe via variável de ambiente JWT_SECRET.
 * @param expiration tempo de vida do access token (ex.: 1h, 30m).
 * @param issuer     emissor gravado na claim {@code iss} e validado em toda requisição.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration expiration, String issuer) {
}
