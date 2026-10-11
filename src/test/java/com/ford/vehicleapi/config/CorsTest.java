package com.ford.vehicleapi.config;

import com.ford.vehicleapi.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("CORS")
class CorsTest extends IntegrationTestSupport {

    private static final String ALLOWED_ORIGIN = "http://localhost:3000";
    private static final String UNKNOWN_ORIGIN = "https://site-malicioso.example";

    @Test
    @DisplayName("preflight de origem autorizada: 200 sem token, com métodos e headers liberados")
    void preflightAllowedOrigin() throws Exception {
        mvc.perform(options("/api/v1/test-drives")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("POST")))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("authorization")))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_MAX_AGE));
    }

    @Test
    @DisplayName("preflight de origem não autorizada: 403")
    void preflightUnknownOrigin() throws Exception {
        mvc.perform(options("/api/v1/models")
                        .header(HttpHeaders.ORIGIN, UNKNOWN_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    @DisplayName("requisição real de origem autorizada recebe Access-Control-Allow-Origin")
    void simpleRequestAllowedOrigin() throws Exception {
        mvc.perform(get("/api/v1/models").header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));
    }

    @Test
    @DisplayName("expõe Location ao front-end na criação de recursos (201)")
    void exposesLocationHeader() throws Exception {
        mvc.perform(post("/api/v1/models").header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .header(authHeader(), bearer(dealerToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Explorer", "category": "SUV", "launchYear": 1990}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, containsString("Location")));
    }
}
