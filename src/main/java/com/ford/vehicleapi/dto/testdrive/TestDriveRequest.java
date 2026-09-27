package com.ford.vehicleapi.dto.testdrive;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record TestDriveRequest(
        @Schema(example = "3", description = "Id da versão (GET /api/v1/models/{modelId}/versions)")
        @NotNull(message = "é obrigatório")
        Long versionId,

        @Schema(example = "2030-01-15T10:00:00")
        @NotNull(message = "é obrigatória") @Future(message = "deve ser uma data futura")
        LocalDateTime scheduledAt,

        @Schema(example = "Ford Caoa - São Paulo")
        @NotBlank(message = "é obrigatória") @Size(max = 120, message = "deve ter no máximo 120 caracteres")
        String dealership,

        @Schema(example = "Gostaria de testar em estrada de terra")
        @Size(max = 500, message = "deve ter no máximo 500 caracteres")
        String notes
) {
}
