package com.ford.vehicleapi.dto.model;

import com.ford.vehicleapi.domain.VehicleCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VehicleModelRequest(
        @Schema(example = "Explorer")
        @NotBlank(message = "é obrigatório") @Size(max = 80, message = "deve ter no máximo 80 caracteres")
        String name,

        @Schema(example = "SUV")
        @NotNull(message = "é obrigatória")
        VehicleCategory category,

        @Schema(example = "SUV grande de 7 lugares")
        @Size(max = 1000, message = "deve ter no máximo 1000 caracteres")
        String description,

        @Schema(example = "1990")
        @NotNull(message = "é obrigatório")
        @Min(value = 1903, message = "deve ser maior ou igual a 1903 (fundação da Ford)")
        @Max(value = 2100, message = "deve ser menor ou igual a 2100")
        Integer launchYear,

        @Schema(example = "true", description = "Se omitido, assume true")
        Boolean active
) {

    public boolean activeOrDefault() {
        return active == null || active;
    }
}
