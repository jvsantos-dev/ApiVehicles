package com.ford.vehicleapi.dto.testdrive;

import com.ford.vehicleapi.domain.TestDriveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record TestDriveStatusRequest(
        @Schema(example = "CONFIRMED")
        @NotNull(message = "é obrigatório")
        TestDriveStatus status
) {
}
