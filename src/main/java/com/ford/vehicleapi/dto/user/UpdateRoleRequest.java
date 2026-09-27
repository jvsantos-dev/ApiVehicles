package com.ford.vehicleapi.dto.user;

import com.ford.vehicleapi.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
        @Schema(example = "DEALER")
        @NotNull(message = "é obrigatório")
        Role role
) {
}
