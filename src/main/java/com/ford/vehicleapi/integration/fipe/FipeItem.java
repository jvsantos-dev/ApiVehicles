package com.ford.vehicleapi.integration.fipe;

/** Par código/nome usado pela FIPE para modelos e anos (ex.: {"codigo": "2025-1", "nome": "2025 Gasolina"}). */
public record FipeItem(String codigo, String nome) {
}
