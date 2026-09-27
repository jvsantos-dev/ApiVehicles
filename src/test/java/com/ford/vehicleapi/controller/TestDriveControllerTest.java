package com.ford.vehicleapi.controller;

import com.ford.vehicleapi.support.IntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("/api/v1/test-drives")
class TestDriveControllerTest extends IntegrationTestSupport {

    private static final String BASE = "/api/v1/test-drives";

    private String requestFor(Long versionId, LocalDateTime when) {
        return """
                {"versionId": %d, "scheduledAt": "%s", "dealership": "Ford Centro - SP", "notes": "Teste"}
                """.formatted(versionId, when.withNano(0));
    }

    private Long schedule(String token, String modelName) throws Exception {
        String response = mvc.perform(post(BASE).header(authHeader(), bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestFor(firstVersion(modelName).getId(), LocalDateTime.now().plusDays(3))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private void changeStatus(Long id, String status, String token, int expectedStatus) throws Exception {
        mvc.perform(patch(BASE + "/{id}/status", id).header(authHeader(), bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"%s\"}".formatted(status)))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    @DisplayName("POST 201: CUSTOMER agenda test drive; dono vem do token, não do corpo")
    void customerSchedules() throws Exception {
        mvc.perform(post(BASE).header(authHeader(), bearer(customerToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestFor(firstVersion("Ranger").getId(), LocalDateTime.now().plusDays(2))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString(BASE + "/")))
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.customerName").value("Cliente Exemplo"))
                .andExpect(jsonPath("$.vehicle").value(containsString("Ford Ranger")));
    }

    @Test
    @DisplayName("POST 401 sem token")
    void scheduleWithoutToken() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(requestFor(firstVersion("Ranger").getId(), LocalDateTime.now().plusDays(2))))
                .andExpect(problem(401));
    }

    @Test
    @DisplayName("POST 403: DEALER não agenda test drive (apenas clientes)")
    void dealerCannotSchedule() throws Exception {
        mvc.perform(post(BASE).header(authHeader(), bearer(dealerToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestFor(firstVersion("Ranger").getId(), LocalDateTime.now().plusDays(2))))
                .andExpect(problem(403));
    }

    @Test
    @DisplayName("POST 400 com data no passado")
    void scheduleInThePast() throws Exception {
        mvc.perform(post(BASE).header(authHeader(), bearer(customerToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestFor(firstVersion("Ranger").getId(), LocalDateTime.now().minusDays(1))))
                .andExpect(problem(400))
                .andExpect(jsonPath("$.errors[0].field").value("scheduledAt"));
    }

    @Test
    @DisplayName("POST 404 com versão inexistente")
    void scheduleUnknownVersion() throws Exception {
        mvc.perform(post(BASE).header(authHeader(), bearer(customerToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestFor(999_999L, LocalDateTime.now().plusDays(1))))
                .andExpect(problem(404));
    }

    @Test
    @DisplayName("POST 422 para modelo fora de linha")
    void scheduleDiscontinuedModel() throws Exception {
        mvc.perform(post(BASE).header(authHeader(), bearer(customerToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestFor(firstVersion("Ka").getId(), LocalDateTime.now().plusDays(1))))
                .andExpect(problem(422));
    }

    @Test
    @DisplayName("GET: CUSTOMER vê só os próprios; DEALER vê todos")
    void listingDependsOnProfile() throws Exception {
        String customer = customerToken();
        String other = registerCustomerAndLogin("outro@email.com");
        schedule(customer, "Ranger");
        schedule(other, "Mustang");

        mvc.perform(get(BASE).header(authHeader(), bearer(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[*].customerName").value(everyItem(is("Cliente Exemplo"))));

        mvc.perform(get(BASE).header(authHeader(), bearer(dealerToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /{id}: 403 quando o test drive é de outro cliente; 200 para o dono e staff")
    void ownershipIsEnforced() throws Exception {
        String owner = customerToken();
        String intruder = registerCustomerAndLogin("intruso@email.com");
        Long id = schedule(owner, "Ranger");

        mvc.perform(get(BASE + "/{id}", id).header(authHeader(), bearer(owner))).andExpect(status().isOk());
        mvc.perform(get(BASE + "/{id}", id).header(authHeader(), bearer(dealerToken()))).andExpect(status().isOk());
        mvc.perform(get(BASE + "/{id}", id).header(authHeader(), bearer(intruder))).andExpect(problem(403));
        mvc.perform(delete(BASE + "/{id}", id).header(authHeader(), bearer(intruder))).andExpect(problem(403));
    }

    @Test
    @DisplayName("GET /{id} 404 para test drive inexistente")
    void notFound() throws Exception {
        mvc.perform(get(BASE + "/{id}", 999_999).header(authHeader(), bearer(adminToken()))).andExpect(problem(404));
    }

    @Test
    @DisplayName("PATCH status: DEALER confirma e conclui; CUSTOMER recebe 403")
    void statusWorkflow() throws Exception {
        String customer = customerToken();
        String dealer = dealerToken();
        Long id = schedule(customer, "Bronco Sport");

        changeStatus(id, "CONFIRMED", customer, 403);
        changeStatus(id, "CONFIRMED", dealer, 200);
        changeStatus(id, "COMPLETED", dealer, 200);

        mvc.perform(get(BASE + "/{id}", id).header(authHeader(), bearer(customer)))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("PATCH status 422 para transição inválida e 400 para status inexistente")
    void invalidStatusTransition() throws Exception {
        Long id = schedule(customerToken(), "Maverick");
        String dealer = dealerToken();

        mvc.perform(patch(BASE + "/{id}/status", id).header(authHeader(), bearer(dealer))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"COMPLETED\"}"))
                .andExpect(problem(422));
        mvc.perform(patch(BASE + "/{id}/status", id).header(authHeader(), bearer(dealer))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"VOANDO\"}"))
                .andExpect(problem(400));
    }

    @Test
    @DisplayName("DELETE 204 pelo dono enquanto REQUESTED; 422 depois de confirmado")
    void ownerCancels() throws Exception {
        String customer = customerToken();
        Long pending = schedule(customer, "Ranger");
        Long confirmed = schedule(customer, "Territory");
        changeStatus(confirmed, "CONFIRMED", dealerToken(), 200);

        mvc.perform(delete(BASE + "/{id}", pending).header(authHeader(), bearer(customer)))
                .andExpect(status().isNoContent());
        mvc.perform(delete(BASE + "/{id}", confirmed).header(authHeader(), bearer(customer)))
                .andExpect(problem(422));
    }

    @Test
    @DisplayName("Modelo com test drive não pode ser excluído (409)")
    void cannotDeleteModelWithTestDrives() throws Exception {
        schedule(customerToken(), "Ranger");
        mvc.perform(delete("/api/v1/models/{id}", model("Ranger").getId()).header(authHeader(), bearer(adminToken())))
                .andExpect(problem(409));
    }
}
