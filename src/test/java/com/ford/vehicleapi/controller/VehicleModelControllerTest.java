package com.ford.vehicleapi.controller;

import com.ford.vehicleapi.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("/api/v1/models")
class VehicleModelControllerTest extends IntegrationTestSupport {

    private static final String BASE = "/api/v1/models";
    private static final String EXPLORER = """
            {"name": "Explorer", "category": "SUV", "description": "SUV de 7 lugares", "launchYear": 1990}
            """;

    @Nested
    @DisplayName("GET (público)")
    class Read {

        @Test
        @DisplayName("200 lista o catálogo sem autenticação")
        void listAll() throws Exception {
            mvc.perform(get(BASE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(9)))
                    .andExpect(jsonPath("$[*].name").value(hasItem("Ranger")))
                    .andExpect(jsonPath("$[*].name").value(hasItem("Mustang")));
        }

        @Test
        @DisplayName("200 filtra por categoria")
        void filterByCategory() throws Exception {
            mvc.perform(get(BASE).param("category", "PICKUP"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].category").value(everyItem(is("PICKUP"))))
                    .andExpect(jsonPath("$[*].name").value(hasItem("Ranger")));
        }

        @Test
        @DisplayName("400 com categoria inexistente")
        void invalidCategory() throws Exception {
            mvc.perform(get(BASE).param("category", "AVIAO")).andExpect(problem(400));
        }

        @Test
        @DisplayName("200 detalha um modelo com preço inicial")
        void findById() throws Exception {
            Long id = model("Ranger").getId();
            mvc.perform(get(BASE + "/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.name").value("Ranger"))
                    .andExpect(jsonPath("$.versionsCount").value(3))
                    .andExpect(jsonPath("$.startingPrice").value(265990.00));
        }

        @Test
        @DisplayName("404 para modelo inexistente")
        void notFound() throws Exception {
            mvc.perform(get(BASE + "/{id}", 999_999)).andExpect(problem(404));
        }

        @Test
        @DisplayName("400 para id com tipo inválido")
        void invalidId() throws Exception {
            mvc.perform(get(BASE + "/abc")).andExpect(problem(400));
        }
    }

    @Nested
    @DisplayName("POST")
    class Create {

        @Test
        @DisplayName("201 como DEALER, com Location apontando para o novo recurso")
        void createAsDealer() throws Exception {
            mvc.perform(post(BASE).header(authHeader(), bearer(dealerToken()))
                            .contentType(MediaType.APPLICATION_JSON).content(EXPLORER))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", endsWith(BASE + "/" + model("Explorer").getId())))
                    .andExpect(jsonPath("$.name").value("Explorer"))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.versionsCount").value(0));
        }

        @Test
        @DisplayName("201 como ADMIN")
        void createAsAdmin() throws Exception {
            mvc.perform(post(BASE).header(authHeader(), bearer(adminToken()))
                            .contentType(MediaType.APPLICATION_JSON).content(EXPLORER))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("401 sem token")
        void createWithoutToken() throws Exception {
            mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(EXPLORER))
                    .andExpect(problem(401));
        }

        @Test
        @DisplayName("403 como CUSTOMER")
        void createAsCustomer() throws Exception {
            mvc.perform(post(BASE).header(authHeader(), bearer(customerToken()))
                            .contentType(MediaType.APPLICATION_JSON).content(EXPLORER))
                    .andExpect(problem(403));
        }

        @Test
        @DisplayName("400 com campos obrigatórios ausentes")
        void createInvalid() throws Exception {
            mvc.perform(post(BASE).header(authHeader(), bearer(dealerToken()))
                            .contentType(MediaType.APPLICATION_JSON).content("""
                                    {"name": "", "launchYear": 1800}
                                    """))
                    .andExpect(problem(400))
                    .andExpect(jsonPath("$.errors[*].field").value(hasItem("name")))
                    .andExpect(jsonPath("$.errors[*].field").value(hasItem("category")))
                    .andExpect(jsonPath("$.errors[*].field").value(hasItem("launchYear")));
        }

        @Test
        @DisplayName("400 com JSON malformado")
        void createMalformedJson() throws Exception {
            mvc.perform(post(BASE).header(authHeader(), bearer(dealerToken()))
                            .contentType(MediaType.APPLICATION_JSON).content("{\"name\": "))
                    .andExpect(problem(400));
        }

        @Test
        @DisplayName("415 com Content-Type não suportado")
        void createUnsupportedMediaType() throws Exception {
            mvc.perform(post(BASE).header(authHeader(), bearer(dealerToken()))
                            .contentType(MediaType.TEXT_PLAIN).content("Explorer"))
                    .andExpect(problem(415));
        }

        @Test
        @DisplayName("409 com nome já existente")
        void createDuplicate() throws Exception {
            mvc.perform(post(BASE).header(authHeader(), bearer(dealerToken()))
                            .contentType(MediaType.APPLICATION_JSON).content("""
                                    {"name": "ranger", "category": "PICKUP", "launchYear": 1983}
                                    """))
                    .andExpect(problem(409));
        }
    }

    @Nested
    @DisplayName("PUT")
    class Update {

        @Test
        @DisplayName("200 como DEALER atualiza o recurso por completo")
        void updateAsDealer() throws Exception {
            Long id = model("Territory").getId();
            mvc.perform(put(BASE + "/{id}", id).header(authHeader(), bearer(dealerToken()))
                            .contentType(MediaType.APPLICATION_JSON).content("""
                                    {"name": "Territory", "category": "SUV", "description": "Nova descrição", "launchYear": 2020, "active": false}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").value("Nova descrição"))
                    .andExpect(jsonPath("$.active").value(false));
        }

        @Test
        @DisplayName("404 para modelo inexistente")
        void updateNotFound() throws Exception {
            mvc.perform(put(BASE + "/{id}", 999_999).header(authHeader(), bearer(dealerToken()))
                            .contentType(MediaType.APPLICATION_JSON).content(EXPLORER))
                    .andExpect(problem(404));
        }

        @Test
        @DisplayName("409 ao renomear para um nome já usado")
        void updateDuplicateName() throws Exception {
            Long id = model("Territory").getId();
            mvc.perform(put(BASE + "/{id}", id).header(authHeader(), bearer(dealerToken()))
                            .contentType(MediaType.APPLICATION_JSON).content("""
                                    {"name": "Mustang", "category": "SUV", "launchYear": 2020}
                                    """))
                    .andExpect(problem(409));
        }

        @Test
        @DisplayName("403 como CUSTOMER")
        void updateAsCustomer() throws Exception {
            Long id = model("Territory").getId();
            mvc.perform(put(BASE + "/{id}", id).header(authHeader(), bearer(customerToken()))
                            .contentType(MediaType.APPLICATION_JSON).content(EXPLORER))
                    .andExpect(problem(403));
        }
    }

    @Nested
    @DisplayName("DELETE")
    class Delete {

        @Test
        @DisplayName("204 como ADMIN e o recurso deixa de existir")
        void deleteAsAdmin() throws Exception {
            Long id = model("Transit").getId();
            mvc.perform(delete(BASE + "/{id}", id).header(authHeader(), bearer(adminToken())))
                    .andExpect(status().isNoContent());
            mvc.perform(get(BASE + "/{id}", id)).andExpect(problem(404));
        }

        @Test
        @DisplayName("403 como DEALER (exclusão é exclusiva do ADMIN)")
        void deleteAsDealer() throws Exception {
            Long id = model("Transit").getId();
            mvc.perform(delete(BASE + "/{id}", id).header(authHeader(), bearer(dealerToken())))
                    .andExpect(problem(403));
        }

        @Test
        @DisplayName("401 sem token")
        void deleteWithoutToken() throws Exception {
            mvc.perform(delete(BASE + "/{id}", model("Transit").getId())).andExpect(problem(401));
        }

        @Test
        @DisplayName("404 para modelo inexistente")
        void deleteNotFound() throws Exception {
            mvc.perform(delete(BASE + "/{id}", 999_999).header(authHeader(), bearer(adminToken())))
                    .andExpect(problem(404));
        }
    }

    @Test
    @DisplayName("405 para método HTTP não suportado no recurso")
    void methodNotAllowed() throws Exception {
        mvc.perform(patch(BASE + "/{id}", model("Ranger").getId()).header(authHeader(), bearer(adminToken()))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(problem(405));
    }
}
