package com.ford.vehicleapi.controller;

import com.ford.vehicleapi.domain.VehicleCategory;
import com.ford.vehicleapi.dto.model.VehicleModelRequest;
import com.ford.vehicleapi.dto.model.VehicleModelResponse;
import com.ford.vehicleapi.service.VehicleModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/models")
@Tag(name = "Modelos", description = "Modelos da linha Ford. Leitura pública; escrita para ADMIN/DEALER; exclusão só ADMIN")
public class VehicleModelController {

    private final VehicleModelService service;

    public VehicleModelController(VehicleModelService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista os modelos do catálogo (público)")
    @ApiResponse(responseCode = "200", description = "Lista de modelos")
    @ApiResponse(responseCode = "400", description = "Categoria inválida")
    public List<VehicleModelResponse> findAll(
            @Parameter(description = "Filtra por categoria") @RequestParam(required = false) VehicleCategory category) {
        return service.findAll(category);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um modelo (público)")
    @ApiResponse(responseCode = "200", description = "Modelo encontrado")
    @ApiResponse(responseCode = "404", description = "Modelo não encontrado")
    public VehicleModelResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra um modelo (ADMIN, DEALER)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "Modelo criado; header Location aponta para o recurso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    @ApiResponse(responseCode = "409", description = "Nome de modelo já existente")
    public ResponseEntity<VehicleModelResponse> create(@Valid @RequestBody VehicleModelRequest request) {
        VehicleModelResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um modelo por completo (ADMIN, DEALER)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Modelo atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    @ApiResponse(responseCode = "404", description = "Modelo não encontrado")
    @ApiResponse(responseCode = "409", description = "Nome de modelo já existente")
    public VehicleModelResponse update(@PathVariable Long id, @Valid @RequestBody VehicleModelRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um modelo e suas versões (ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "Modelo removido")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    @ApiResponse(responseCode = "404", description = "Modelo não encontrado")
    @ApiResponse(responseCode = "409", description = "Modelo possui test drives")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
