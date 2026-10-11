package com.ford.vehicleapi.controller;

import com.ford.vehicleapi.exception.ExternalServiceException;
import com.ford.vehicleapi.exception.ResourceNotFoundException;
import com.ford.vehicleapi.integration.fipe.FipeClient;
import com.ford.vehicleapi.integration.fipe.FipeItem;
import com.ford.vehicleapi.integration.fipe.FipePricePayload;
import com.ford.vehicleapi.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Endpoints da integração com a FIPE. O cliente HTTP é simulado para os testes não dependerem da internet. */
@DisplayName("/api/v1/fipe/models (web service externo)")
class FipeControllerTest extends IntegrationTestSupport {

    private static final String BASE = "/api/v1/fipe/models";

    @MockitoBean
    private FipeClient fipeClient;

    @Test
    @DisplayName("200 lista modelos sem autenticação, filtrando pelo nome")
    void listModelsFilteredByName() throws Exception {
        given(fipeClient.listModels()).willReturn(List.of(
                new FipeItem("10001", "Ranger Limited 3.0 V6 4x4 Aut."),
                new FipeItem("11489", "Bronco Sport Badlands 2.0L TB 4WD Aut."),
                new FipeItem("10002", "RANGER XLS 2.0 Diesel")));

        mvc.perform(get(BASE).param("name", "ranger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("10001"))
                .andExpect(jsonPath("$[1].name").value("RANGER XLS 2.0 Diesel"));
    }

    @Test
    @DisplayName("200 lista os anos de um modelo")
    void listYears() throws Exception {
        given(fipeClient.listYears("11489")).willReturn(List.of(new FipeItem("2025-1", "2025 Gasolina")));

        mvc.perform(get(BASE + "/{model}/years", "11489"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("2025-1"))
                .andExpect(jsonPath("$[0].name").value("2025 Gasolina"));
    }

    @Test
    @DisplayName("200 devolve o preço FIPE convertido de \"R$ 217.609,00\" para número")
    void price() throws Exception {
        given(fipeClient.getPrice("11489", "2025-1")).willReturn(new FipePricePayload("R$ 217.609,00", "Ford",
                "Bronco Sport Badlands 2.0L TB 4WD Aut.", 2025, "Gasolina", "003513-0", "outubro de 2026"));

        mvc.perform(get(BASE + "/{model}/years/{year}", "11489", "2025-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fipeCode").value("003513-0"))
                .andExpect(jsonPath("$.brand").value("Ford"))
                .andExpect(jsonPath("$.modelYear").value(2025))
                .andExpect(jsonPath("$.price").value(217609.00))
                .andExpect(jsonPath("$.referenceMonth").value("outubro de 2026"));
    }

    @Test
    @DisplayName("404 quando o modelo não existe na FIPE")
    void notFound() throws Exception {
        given(fipeClient.listYears("999999")).willThrow(new ResourceNotFoundException("Modelo FIPE", "999999"));

        mvc.perform(get(BASE + "/{model}/years", "999999")).andExpect(problem(404));
    }

    @Test
    @DisplayName("502 no formato Problem Details quando a FIPE está fora do ar")
    void badGateway() throws Exception {
        given(fipeClient.listModels()).willThrow(new ExternalServiceException("A Tabela FIPE está indisponível", null));

        mvc.perform(get(BASE))
                .andExpect(problem(502))
                .andExpect(jsonPath("$.title").value("Serviço externo indisponível"));
    }
}
