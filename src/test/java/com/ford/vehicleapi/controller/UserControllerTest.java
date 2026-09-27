package com.ford.vehicleapi.controller;

import com.ford.vehicleapi.repository.UserRepository;
import com.ford.vehicleapi.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("/api/v1/users (ADMIN)")
class UserControllerTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    private Long userId(String email) {
        return userRepository.findByEmailIgnoreCase(email).orElseThrow().getId();
    }

    @Test
    @DisplayName("GET 200 como ADMIN, sem expor senhas")
    void listAsAdmin() throws Exception {
        mvc.perform(get("/api/v1/users").header(authHeader(), bearer(adminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email").value(hasItem(DEALER_EMAIL)))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    @DisplayName("GET 403 como DEALER e CUSTOMER; 401 sem token")
    void forbiddenForOtherProfiles() throws Exception {
        mvc.perform(get("/api/v1/users").header(authHeader(), bearer(dealerToken()))).andExpect(problem(403));
        mvc.perform(get("/api/v1/users").header(authHeader(), bearer(customerToken()))).andExpect(problem(403));
        mvc.perform(get("/api/v1/users")).andExpect(problem(401));
    }

    @Test
    @DisplayName("GET /{id} 404 para usuário inexistente")
    void notFound() throws Exception {
        mvc.perform(get("/api/v1/users/{id}", 999_999).header(authHeader(), bearer(adminToken())))
                .andExpect(problem(404));
    }

    @Test
    @DisplayName("PATCH role: promove cliente a DEALER; novo perfil vale no próximo token")
    void promoteCustomer() throws Exception {
        String oldToken = registerCustomerAndLogin("vendedor@email.com");
        Long id = userId("vendedor@email.com");

        mvc.perform(patch("/api/v1/users/{id}/role", id).header(authHeader(), bearer(adminToken()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\": \"DEALER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("DEALER"));

        String modelJson = """
                {"name": "Everest", "category": "SUV", "launchYear": 2003}
                """;
        // Token antigo ainda carrega o perfil CUSTOMER (JWT é stateless) -> 403
        mvc.perform(post("/api/v1/models").header(authHeader(), bearer(oldToken))
                        .contentType(MediaType.APPLICATION_JSON).content(modelJson))
                .andExpect(problem(403));
        // Novo login gera token com perfil DEALER -> 201
        mvc.perform(post("/api/v1/models").header(authHeader(), bearer(login("vendedor@email.com", "Senha1234")))
                        .contentType(MediaType.APPLICATION_JSON).content(modelJson))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PATCH role 422 quando o ADMIN tenta alterar o próprio perfil")
    void adminCannotDemoteSelf() throws Exception {
        mvc.perform(patch("/api/v1/users/{id}/role", userId(ADMIN_EMAIL)).header(authHeader(), bearer(adminToken()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\": \"CUSTOMER\"}"))
                .andExpect(problem(422));
    }

    @Test
    @DisplayName("Rota inexistente retorna 404 no formato padrão")
    void unknownRoute() throws Exception {
        mvc.perform(get("/api/v1/nao-existe").header(authHeader(), bearer(adminToken())))
                .andExpect(problem(404));
    }
}
