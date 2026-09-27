package com.ford.vehicleapi.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "admin@ford.com")
        @NotBlank(message = "é obrigatório") @Email(message = "deve ser um e-mail válido")
        String email,

        @Schema(example = "Admin@123")
        @NotBlank(message = "é obrigatória")
        String password
) {
}
