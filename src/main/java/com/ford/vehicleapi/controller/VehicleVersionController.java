package com.ford.vehicleapi.controller;

import com.ford.vehicleapi.dto.version.VehicleVersionRequest;
import com.ford.vehicleapi.dto.version.VehicleVersionResponse;
import com.ford.vehicleapi.service.VehicleVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** Versões são um sub-recurso de modelo: /models/{modelId}/versions/{versionId}. */
@RestController
@RequestMapping("/api/v1/models/{modelId}/versions")
@Tag(name = "Versões", description = "Versões (trims) de cada modelo. Leitura pública; escrita para ADMIN/DEALER; exclusão só ADMIN")
public class VehicleVersionController {

    private final VehicleVersionService service;

    public VehicleVersionController(VehicleVersionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista as versões de um modelo, da mais barata para a mais cara (público)")
    @ApiResponse(responseCode = "200", description = "Lista de versões")
    @ApiResponse(responseCode = "404", description = "Modelo não encontrado")
    public List<VehicleVersionResponse> findAll(@PathVariable Long modelId) {
        return service.findByModel(modelId);
    }

    @GetMapping("/{versionId}")
    @Operation(summary = "Detalha uma versão (público)")
    @ApiResponse(responseCode = "200", description = "Versão encontrada")
    @ApiResponse(responseCode = "404", description = "Modelo ou versão não encontrado")
    public VehicleVersionResponse findById(@PathVariable Long modelId, @PathVariable Long versionId) {
        return service.findById(modelId, versionId);
    }

    @PostMapping
    @Operation(summary = "Cadastra uma versão no modelo (ADMIN, DEALER)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "Versão criada; header Location aponta para o recurso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    @ApiResponse(responseCode = "404", description = "Modelo não encontrado")
    @ApiResponse(responseCode = "409", description = "Versão/ano já existente no modelo")
    public ResponseEntity<VehicleVersionResponse> create(@PathVariable Long modelId,
                                                         @Valid @RequestBody VehicleVersionRequest request) {
        VehicleVersionResponse created = service.create(modelId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{versionId}")
    @Operation(summary = "Atualiza uma versão por completo (ADMIN, DEALER)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Versão atualizada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    @ApiResponse(responseCode = "404", description = "Modelo ou versão não encontrado")
    @ApiResponse(responseCode = "409", description = "Versão/ano já existente no modelo")
    public VehicleVersionResponse update(@PathVariable Long modelId, @PathVariable Long versionId,
                                         @Valid @RequestBody VehicleVersionRequest request) {
        return service.update(modelId, versionId, request);
    }

    @DeleteMapping("/{versionId}")
    @Operation(summary = "Remove uma versão (ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "Versão removida")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    @ApiResponse(responseCode = "404", description = "Modelo ou versão não encontrado")
    @ApiResponse(responseCode = "409", description = "Versão possui test drives")
    public ResponseEntity<Void> delete(@PathVariable Long modelId, @PathVariable Long versionId) {
        service.delete(modelId, versionId);
        return ResponseEntity.noContent().build();
    }
}
