package com.ford.vehicleapi.controller;

import com.ford.vehicleapi.dto.testdrive.TestDriveRequest;
import com.ford.vehicleapi.dto.testdrive.TestDriveResponse;
import com.ford.vehicleapi.dto.testdrive.TestDriveStatusRequest;
import com.ford.vehicleapi.security.CurrentUser;
import com.ford.vehicleapi.service.TestDriveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/test-drives")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Test drives", description = "Agendamentos de test drive. CUSTOMER agenda e vê os próprios; ADMIN/DEALER gerenciam todos")
public class TestDriveController {

    private final TestDriveService service;

    public TestDriveController(TestDriveService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista test drives: CUSTOMER vê só os seus; ADMIN/DEALER veem todos")
    @ApiResponse(responseCode = "200", description = "Lista de test drives")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    public List<TestDriveResponse> findAll(@AuthenticationPrincipal Jwt jwt) {
        return service.findAll(CurrentUser.from(jwt));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um test drive (dono, ADMIN ou DEALER)")
    @ApiResponse(responseCode = "200", description = "Test drive encontrado")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "403", description = "Test drive de outro cliente")
    @ApiResponse(responseCode = "404", description = "Test drive não encontrado")
    public TestDriveResponse findById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.findById(id, CurrentUser.from(jwt));
    }

    @PostMapping
    @Operation(summary = "Agenda um test drive para o cliente logado (CUSTOMER)")
    @ApiResponse(responseCode = "201", description = "Test drive agendado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos (ex.: data no passado)")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "403", description = "Apenas clientes podem agendar")
    @ApiResponse(responseCode = "404", description = "Versão não encontrada")
    @ApiResponse(responseCode = "422", description = "Modelo fora de linha")
    public ResponseEntity<TestDriveResponse> create(@Valid @RequestBody TestDriveRequest request,
                                                    @AuthenticationPrincipal Jwt jwt) {
        TestDriveResponse created = service.create(request, CurrentUser.from(jwt));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Altera o status do test drive (ADMIN, DEALER)")
    @ApiResponse(responseCode = "200", description = "Status alterado")
    @ApiResponse(responseCode = "400", description = "Status inválido")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    @ApiResponse(responseCode = "404", description = "Test drive não encontrado")
    @ApiResponse(responseCode = "422", description = "Transição de status não permitida")
    public TestDriveResponse updateStatus(@PathVariable Long id, @Valid @RequestBody TestDriveStatusRequest request) {
        return service.updateStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancela/remove um agendamento (dono enquanto REQUESTED, ou ADMIN)")
    @ApiResponse(responseCode = "204", description = "Test drive removido")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "403", description = "Test drive de outro cliente")
    @ApiResponse(responseCode = "404", description = "Test drive não encontrado")
    @ApiResponse(responseCode = "422", description = "Status não permite remoção")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        service.delete(id, CurrentUser.from(jwt));
        return ResponseEntity.noContent().build();
    }
}
