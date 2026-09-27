package com.ford.vehicleapi.controller;

import com.ford.vehicleapi.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("/api/v1/models/{modelId}/versions")
class VehicleVersionControllerTest extends IntegrationTestSupport {

    private static final String BASE = "/api/v1/models/{modelId}/versions";
    private static final String STORM = """
            {"name": "Storm", "modelYear": 2026, "engine": "3.0 V6 Turbo Diesel", "horsepower": 250,
             "transmission": "Automática 10 marchas", "fuelType": "DIESEL", "price": 359990.00}
            """;

    @Test
    @DisplayName("GET 200 lista versões ordenadas por preço, sem autenticação")
    void listVersions() throws Exception {
        mvc.perform(get(BASE, model("Ranger").getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("XLS"))
                .andExpect(jsonPath("$[2].name").value("Raptor"))
                .andExpect(jsonPath("$[0].modelName").value("Ranger"));
    }

    @Test
    @DisplayName("GET 404 quando o modelo não existe")
    void listVersionsOfUnknownModel() throws Exception {
        mvc.perform(get(BASE, 999_999)).andExpect(problem(404));
    }

    @Test
    @DisplayName("GET 404 quando a versão pertence a outro modelo")
    void versionFromAnotherModel() throws Exception {
        Long mustangVersion = firstVersion("Mustang").getId();
        mvc.perform(get(BASE + "/{versionId}", model("Ranger").getId(), mustangVersion))
                .andExpect(problem(404));
    }

    @Test
    @DisplayName("POST 201 como DEALER, com Location")
    void createAsDealer() throws Exception {
        Long rangerId = model("Ranger").getId();
        mvc.perform(post(BASE, rangerId).header(authHeader(), bearer(dealerToken()))
                        .contentType(MediaType.APPLICATION_JSON).content(STORM))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/models/" + rangerId + "/versions/")))
                .andExpect(jsonPath("$.name").value("Storm"))
                .andExpect(jsonPath("$.modelId").value(rangerId));

        mvc.perform(get(BASE, rangerId)).andExpect(jsonPath("$[*].name").value(hasItem("Storm")));
    }

    @Test
    @DisplayName("POST 403 como CUSTOMER")
    void createAsCustomer() throws Exception {
        mvc.perform(post(BASE, model("Ranger").getId()).header(authHeader(), bearer(customerToken()))
                        .contentType(MediaType.APPLICATION_JSON).content(STORM))
                .andExpect(problem(403));
    }

    @Test
    @DisplayName("POST 400 com preço negativo e combustível inválido")
    void createInvalid() throws Exception {
        mvc.perform(post(BASE, model("Ranger").getId()).header(authHeader(), bearer(dealerToken()))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"name": "X", "modelYear": 2026, "engine": "1.0", "horsepower": 0,
                                 "transmission": "Manual", "fuelType": "DIESEL", "price": -1}
                                """))
                .andExpect(problem(400))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("price")))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("horsepower")));
    }

    @Test
    @DisplayName("POST 409 com versão/ano já existentes no modelo")
    void createDuplicate() throws Exception {
        mvc.perform(post(BASE, model("Ranger").getId()).header(authHeader(), bearer(dealerToken()))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"name": "Raptor", "modelYear": 2026, "engine": "3.0 V6", "horsepower": 397,
                                 "transmission": "Automática", "fuelType": "GASOLINE", "price": 1}
                                """))
                .andExpect(problem(409));
    }

    @Test
    @DisplayName("PUT 200 atualiza preço da versão")
    void update() throws Exception {
        var version = firstVersion("Territory");
        mvc.perform(put(BASE + "/{versionId}", model("Territory").getId(), version.getId())
                        .header(authHeader(), bearer(adminToken()))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"name": "Titanium", "modelYear": 2026, "engine": "1.5 EcoBoost", "horsepower": 169,
                                 "transmission": "DCT 7 marchas", "fuelType": "GASOLINE", "price": 199990.00}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(199990.00));
    }

    @Test
    @DisplayName("DELETE 204 como ADMIN; 403 como DEALER")
    void deleteVersion() throws Exception {
        Long modelId = model("Maverick").getId();
        Long versionId = firstVersion("Maverick").getId();

        mvc.perform(delete(BASE + "/{versionId}", modelId, versionId).header(authHeader(), bearer(dealerToken())))
                .andExpect(problem(403));
        mvc.perform(delete(BASE + "/{versionId}", modelId, versionId).header(authHeader(), bearer(adminToken())))
                .andExpect(status().isNoContent());
        mvc.perform(get(BASE + "/{versionId}", modelId, versionId)).andExpect(problem(404));
    }
}
