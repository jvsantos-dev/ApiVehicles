package com.ford.vehicleapi.integration.fipe;

import java.util.List;

/** Resposta de GET /carros/marcas/{marca}/modelos. */
public record FipeModelsPayload(List<FipeItem> modelos) {
}
