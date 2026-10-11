package com.ford.vehicleapi.integration.fipe;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Resposta de GET /carros/marcas/{marca}/modelos/{modelo}/anos/{ano} (campos em PascalCase na FIPE). */
public record FipePricePayload(
        @JsonProperty("Valor") String valor,
        @JsonProperty("Marca") String marca,
        @JsonProperty("Modelo") String modelo,
        @JsonProperty("AnoModelo") Integer anoModelo,
        @JsonProperty("Combustivel") String combustivel,
        @JsonProperty("CodigoFipe") String codigoFipe,
        @JsonProperty("MesReferencia") String mesReferencia
) {
}
