package com.ford.vehicleapi.integration.fipe;

import com.ford.vehicleapi.exception.ExternalServiceException;
import com.ford.vehicleapi.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.function.Supplier;

/**
 * Cliente HTTP da API pública da Tabela FIPE (https://deividfortuna.github.io/fipe/).
 * <p>
 * Traduz as falhas do serviço externo para as exceções da API: recurso inexistente vira 404
 * e indisponibilidade (timeout, 5xx, limite de requisições) vira 502.
 */
@Component
public class FipeClient {

    private static final Logger log = LoggerFactory.getLogger(FipeClient.class);
    private static final String MODELS = "/carros/marcas/{brand}/modelos";
    private static final String UNAVAILABLE = "A Tabela FIPE está indisponível no momento. Tente novamente mais tarde.";

    private final RestClient restClient;
    private final String brandCode;

    public FipeClient(RestClient fipeRestClient, FipeProperties properties) {
        this.restClient = fipeRestClient;
        this.brandCode = properties.brandCode();
    }

    public List<FipeItem> listModels() {
        FipeModelsPayload payload = call("Marca FIPE", brandCode, () -> restClient.get()
                .uri(MODELS, brandCode)
                .retrieve()
                .body(FipeModelsPayload.class));
        return payload == null || payload.modelos() == null ? List.of() : payload.modelos();
    }

    public List<FipeItem> listYears(String modelCode) {
        List<FipeItem> years = call("Modelo FIPE", modelCode, () -> restClient.get()
                .uri(MODELS + "/{model}/anos", brandCode, modelCode)
                .retrieve()
                .body(new ParameterizedTypeReference<List<FipeItem>>() {
                }));
        return years == null ? List.of() : years;
    }

    public FipePricePayload getPrice(String modelCode, String yearCode) {
        String id = modelCode + "/" + yearCode;
        FipePricePayload price = call("Preço FIPE", id, () -> restClient.get()
                .uri(MODELS + "/{model}/anos/{year}", brandCode, modelCode, yearCode)
                .retrieve()
                .body(FipePricePayload.class));
        if (price == null) {
            throw new ResourceNotFoundException("Preço FIPE", id);
        }
        return price;
    }

    private <T> T call(String resource, String id, Supplier<T> request) {
        try {
            return request.get();
        } catch (RestClientResponseException ex) {
            if (isNotFound(ex)) {
                throw new ResourceNotFoundException(resource, id);
            }
            log.warn("FIPE respondeu {} para {} {}", ex.getStatusCode(), resource, id);
            throw new ExternalServiceException(UNAVAILABLE, ex);
        } catch (RestClientException ex) {
            log.warn("Falha ao consultar a FIPE ({} {}): {}", resource, id, ex.getMessage());
            throw new ExternalServiceException(UNAVAILABLE, ex);
        }
    }

    /**
     * A FIPE responde 4xx para códigos inválidos, mas para modelo/ano inexistente devolve
     * 500 com o corpo {"error": "recurso não encontrado..."}. Limite de requisições (429) é indisponibilidade.
     */
    private static boolean isNotFound(RestClientResponseException ex) {
        if (ex.getStatusCode().isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS)) {
            return false;
        }
        return ex.getStatusCode().is4xxClientError() || ex.getResponseBodyAsString().contains("\"error\"");
    }
}
