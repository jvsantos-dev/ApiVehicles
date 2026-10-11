package com.ford.vehicleapi.integration.fipe;

import com.ford.vehicleapi.exception.ExternalServiceException;
import com.ford.vehicleapi.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Testes unitários do cliente da FIPE: servidor HTTP simulado, sem acessar a internet. */
@DisplayName("Cliente da Tabela FIPE (unitário)")
class FipeClientTest {

    private static final String BASE = "https://fipe.test/api/v1";
    private static final String MODELS = BASE + "/carros/marcas/22/modelos";

    private MockRestServiceServer server;
    private FipeClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new FipeClient(builder.build(), new FipeProperties(BASE, "22", Duration.ofSeconds(1), Duration.ofSeconds(1)));
    }

    @Test
    @DisplayName("lista os modelos da marca Ford (código numérico vira texto)")
    void listModels() {
        server.expect(requestTo(MODELS)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"modelos": [{"codigo": 11489, "nome": "Bronco Sport Badlands 2.0L TB 4WD Aut."}],
                         "anos": [{"codigo": "2025-1", "nome": "2025 Gasolina"}]}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.listModels())
                .containsExactly(new FipeItem("11489", "Bronco Sport Badlands 2.0L TB 4WD Aut."));
        server.verify();
    }

    @Test
    @DisplayName("lista os anos de um modelo")
    void listYears() {
        server.expect(requestTo(MODELS + "/11489/anos"))
                .andRespond(withSuccess("""
                        [{"codigo": "2026-1", "nome": "2026 Gasolina"}, {"codigo": "2025-1", "nome": "2025 Gasolina"}]
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.listYears("11489")).extracting(FipeItem::codigo).containsExactly("2026-1", "2025-1");
    }

    @Test
    @DisplayName("lê o preço com os campos em PascalCase da FIPE")
    void getPrice() {
        server.expect(requestTo(MODELS + "/11489/anos/2025-1"))
                .andRespond(withSuccess("""
                        {"TipoVeiculo": 1, "Valor": "R$ 217.609,00", "Marca": "Ford",
                         "Modelo": "Bronco Sport Badlands 2.0L TB 4WD Aut.", "AnoModelo": 2025, "Combustivel": "Gasolina",
                         "CodigoFipe": "003513-0", "MesReferencia": "outubro de 2026", "SiglaCombustivel": "G"}
                        """, MediaType.APPLICATION_JSON));

        FipePricePayload price = client.getPrice("11489", "2025-1");

        assertThat(price.valor()).isEqualTo("R$ 217.609,00");
        assertThat(price.codigoFipe()).isEqualTo("003513-0");
        assertThat(price.anoModelo()).isEqualTo(2025);
    }

    @Test
    @DisplayName("modelo inexistente (FIPE responde 500 com {\"error\"}) vira ResourceNotFoundException")
    void notFound() {
        server.expect(requestTo(MODELS + "/999999/anos"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\": \"recurso não encontrado para a referência informada\"}"));

        assertThatThrownBy(() -> client.listYears("999999")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("FIPE fora do ar (503) vira ExternalServiceException")
    void unavailable() {
        server.expect(requestTo(MODELS)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> client.listModels()).isInstanceOf(ExternalServiceException.class);
    }

    @Test
    @DisplayName("limite de requisições (429) vira ExternalServiceException, não 404")
    void rateLimited() {
        server.expect(requestTo(MODELS)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> client.listModels()).isInstanceOf(ExternalServiceException.class);
    }
}
