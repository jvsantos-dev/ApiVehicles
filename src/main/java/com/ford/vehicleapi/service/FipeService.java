package com.ford.vehicleapi.service;

import com.ford.vehicleapi.dto.fipe.FipeModelResponse;
import com.ford.vehicleapi.dto.fipe.FipePriceResponse;
import com.ford.vehicleapi.dto.fipe.FipeYearResponse;
import com.ford.vehicleapi.exception.ExternalServiceException;
import com.ford.vehicleapi.integration.fipe.FipeClient;
import com.ford.vehicleapi.integration.fipe.FipePricePayload;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/** Consulta a Tabela FIPE e converte o formato dela (português, valores em texto) para os contratos da API. */
@Service
public class FipeService {

    private final FipeClient client;

    public FipeService(FipeClient client) {
        this.client = client;
    }

    public List<FipeModelResponse> findModels(String name) {
        String filter = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        return client.listModels().stream()
                .filter(item -> item.nome().toLowerCase(Locale.ROOT).contains(filter))
                .map(item -> new FipeModelResponse(item.codigo(), item.nome()))
                .toList();
    }

    public List<FipeYearResponse> findYears(String modelCode) {
        return client.listYears(modelCode).stream()
                .map(item -> new FipeYearResponse(item.codigo(), item.nome()))
                .toList();
    }

    public FipePriceResponse findPrice(String modelCode, String yearCode) {
        FipePricePayload payload = client.getPrice(modelCode, yearCode);
        return new FipePriceResponse(payload.codigoFipe(), payload.marca(), payload.modelo(), payload.anoModelo(),
                payload.combustivel(), parsePrice(payload.valor()), payload.mesReferencia());
    }

    /** "R$ 217.609,00" -> 217609.00 */
    static BigDecimal parsePrice(String value) {
        try {
            String number = value.replace("R$", "").replace(".", "").replace(",", ".").trim();
            return new BigDecimal(number);
        } catch (RuntimeException ex) {
            throw new ExternalServiceException("A Tabela FIPE retornou um valor em formato inesperado: " + value, ex);
        }
    }
}
