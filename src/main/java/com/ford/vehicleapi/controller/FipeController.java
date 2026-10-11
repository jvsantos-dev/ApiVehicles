package com.ford.vehicleapi.controller;

import com.ford.vehicleapi.dto.fipe.FipeModelResponse;
import com.ford.vehicleapi.dto.fipe.FipePriceResponse;
import com.ford.vehicleapi.dto.fipe.FipeYearResponse;
import com.ford.vehicleapi.service.FipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Consumo de web service externo: preço de mercado dos veículos Ford na Tabela FIPE. */
@RestController
@RequestMapping("/api/v1/fipe/models")
@Tag(name = "Tabela FIPE", description = "Integração com a API pública da Tabela FIPE: preço médio de mercado dos veículos Ford (público)")
public class FipeController {

    private final FipeService service;

    public FipeController(FipeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista os modelos Ford cadastrados na FIPE (público)")
    @ApiResponse(responseCode = "200", description = "Lista de modelos")
    @ApiResponse(responseCode = "502", description = "Tabela FIPE indisponível")
    public List<FipeModelResponse> findModels(
            @Parameter(description = "Filtra pelo nome (contém, sem diferenciar maiúsculas), ex.: ranger")
            @RequestParam(required = false) String name) {
        return service.findModels(name);
    }

    @GetMapping("/{modelCode}/years")
    @Operation(summary = "Lista os anos-modelo disponíveis de um modelo FIPE (público)")
    @ApiResponse(responseCode = "200", description = "Lista de anos")
    @ApiResponse(responseCode = "404", description = "Modelo não encontrado na FIPE")
    @ApiResponse(responseCode = "502", description = "Tabela FIPE indisponível")
    public List<FipeYearResponse> findYears(@Parameter(example = "11489") @PathVariable String modelCode) {
        return service.findYears(modelCode);
    }

    @GetMapping("/{modelCode}/years/{yearCode}")
    @Operation(summary = "Consulta o preço médio de mercado de um modelo/ano na FIPE (público)")
    @ApiResponse(responseCode = "200", description = "Preço FIPE")
    @ApiResponse(responseCode = "404", description = "Modelo ou ano não encontrado na FIPE")
    @ApiResponse(responseCode = "502", description = "Tabela FIPE indisponível")
    public FipePriceResponse findPrice(@Parameter(example = "11489") @PathVariable String modelCode,
                                       @Parameter(example = "2025-1") @PathVariable String yearCode) {
        return service.findPrice(modelCode, yearCode);
    }
}
