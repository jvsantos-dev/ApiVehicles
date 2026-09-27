package com.ford.vehicleapi.support;

import com.ford.vehicleapi.domain.VehicleModel;
import com.ford.vehicleapi.domain.VehicleVersion;
import com.ford.vehicleapi.repository.VehicleModelRepository;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base dos testes de integração: sobe a aplicação completa (filtros de segurança, JWT real,
 * H2 em memória com os dados de exemplo) e desfaz as alterações ao fim de cada teste.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class IntegrationTestSupport {

    protected static final String ADMIN_EMAIL = "admin@ford.com";
    protected static final String DEALER_EMAIL = "dealer@ford.com";
    protected static final String CUSTOMER_EMAIL = "customer@ford.com";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected VehicleModelRepository modelRepository;

    protected String adminToken() throws Exception {
        return login(ADMIN_EMAIL, "Admin@123");
    }

    protected String dealerToken() throws Exception {
        return login(DEALER_EMAIL, "Dealer@123");
    }

    protected String customerToken() throws Exception {
        return login(CUSTOMER_EMAIL, "Customer@123");
    }

    /** Faz login pela API real e devolve o access token. */
    protected String login(String email, String password) throws Exception {
        String body = """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
        String response = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.accessToken");
    }

    /** Cadastra um novo cliente e devolve seu token. */
    protected String registerCustomerAndLogin(String email) throws Exception {
        String body = """
                {"name": "Cliente Teste", "email": "%s", "password": "Senha1234"}
                """.formatted(email);
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        return login(email, "Senha1234");
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }

    protected static String authHeader() {
        return HttpHeaders.AUTHORIZATION;
    }

    protected VehicleModel model(String name) {
        return modelRepository.findAllByOrderByNameAsc().stream()
                .filter(m -> m.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    protected VehicleVersion firstVersion(String modelName) {
        return model(modelName).getVersions().get(0);
    }

    /** Verifica o formato padronizado de erro (Problem Details). */
    protected static ResultMatcher problem(int status) {
        return result -> {
            status().is(status).match(result);
            content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON).match(result);
            jsonPath("$.status").value(status).match(result);
            jsonPath("$.title").isNotEmpty().match(result);
            jsonPath("$.detail").isNotEmpty().match(result);
            jsonPath("$.type").isNotEmpty().match(result);
            jsonPath("$.instance").value(result.getRequest().getRequestURI()).match(result);
            jsonPath("$.timestamp").isNotEmpty().match(result);
        };
    }
}
