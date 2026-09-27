package com.ford.vehicleapi.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Cadastro público: sempre cria um usuário com perfil CUSTOMER. */
public record RegisterRequest(
        @Schema(example = "Maria Silva")
        @NotBlank(message = "é obrigatório") @Size(max = 120, message = "deve ter no máximo 120 caracteres")
        String name,

        @Schema(example = "maria@email.com")
        @NotBlank(message = "é obrigatório") @Email(message = "deve ser um e-mail válido")
        @Size(max = 160, message = "deve ter no máximo 160 caracteres")
        String email,

        @Schema(example = "Senha@123", description = "Mínimo 8 caracteres, com letras e números")
        @NotBlank(message = "é obrigatória")
        @Size(min = 8, max = 72, message = "deve ter entre 8 e 72 caracteres")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "deve conter letras e números")
        String password
) {
}
