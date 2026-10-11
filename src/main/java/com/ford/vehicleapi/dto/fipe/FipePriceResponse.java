package com.ford.vehicleapi.dto.fipe;

import java.math.BigDecimal;

/** Preço médio de mercado segundo a Tabela FIPE, já convertido para número. */
public record FipePriceResponse(
        String fipeCode,
        String brand,
        String model,
        Integer modelYear,
        String fuel,
        BigDecimal price,
        String referenceMonth
) {
}
