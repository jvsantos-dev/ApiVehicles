package com.ford.vehicleapi.dto.fipe;

/** Ano-modelo/combustível disponível na FIPE (ex.: code "2025-1", name "2025 Gasolina"). */
public record FipeYearResponse(String code, String name) {
}
