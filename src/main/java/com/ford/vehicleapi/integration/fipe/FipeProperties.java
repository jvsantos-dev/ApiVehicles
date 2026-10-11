package com.ford.vehicleapi.integration.fipe;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuração do cliente da Tabela FIPE (prefixo {@code app.fipe}).
 *
 * @param baseUrl        URL base da API pública da FIPE (Parallelum).
 * @param brandCode      código da marca na FIPE (22 = Ford, carros).
 * @param connectTimeout tempo máximo para abrir a conexão.
 * @param readTimeout    tempo máximo esperando a resposta.
 */
@ConfigurationProperties(prefix = "app.fipe")
public record FipeProperties(String baseUrl, String brandCode, Duration connectTimeout, Duration readTimeout) {
}
