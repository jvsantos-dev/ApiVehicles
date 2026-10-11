package com.ford.vehicleapi.config;

import com.ford.vehicleapi.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Documentação OpenAPI / Swagger")
class OpenApiDocsTest extends IntegrationTestSupport {

    @Test
    @DisplayName("/v3/api-docs é público e documenta todos os recursos, inclusive a integração FIPE")
    void apiDocs() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Ford Vehicle Catalog API"))
                .andExpect(jsonPath("$.paths['/api/v1/models']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/models/{modelId}/versions']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/test-drives']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/users']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/auth/login']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/fipe/models/{modelCode}/years/{yearCode}']").exists())
                .andExpect(jsonPath("$.tags[*].name").value(hasItem("Tabela FIPE")))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
    }

    @Test
    @DisplayName("Swagger UI é acessível sem autenticação")
    void swaggerUi() throws Exception {
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }
}
