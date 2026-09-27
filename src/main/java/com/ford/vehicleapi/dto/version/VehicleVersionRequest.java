package com.ford.vehicleapi.dto.version;

import com.ford.vehicleapi.domain.FuelType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record VehicleVersionRequest(
        @Schema(example = "Limited")
        @NotBlank(message = "é obrigatório") @Size(max = 80, message = "deve ter no máximo 80 caracteres")
        String name,

        @Schema(example = "2026")
        @NotNull(message = "é obrigatório")
        @Min(value = 1903, message = "deve ser maior ou igual a 1903")
        @Max(value = 2100, message = "deve ser menor ou igual a 2100")
        Integer modelYear,

        @Schema(example = "3.0 V6 Turbo Diesel")
        @NotBlank(message = "é obrigatório") @Size(max = 80, message = "deve ter no máximo 80 caracteres")
        String engine,

        @Schema(example = "250")
        @NotNull(message = "é obrigatório") @Positive(message = "deve ser positiva")
        Integer horsepower,

        @Schema(example = "Automática 10 marchas")
        @NotBlank(message = "é obrigatória") @Size(max = 60, message = "deve ter no máximo 60 caracteres")
        String transmission,

        @Schema(example = "DIESEL")
        @NotNull(message = "é obrigatório")
        FuelType fuelType,

        @Schema(example = "339990.00")
        @NotNull(message = "é obrigatório")
        @DecimalMin(value = "0.01", message = "deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "deve ter no máximo 10 dígitos inteiros e 2 decimais")
        BigDecimal price
) {
}
