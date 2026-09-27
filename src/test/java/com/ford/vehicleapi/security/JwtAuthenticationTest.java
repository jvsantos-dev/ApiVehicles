package com.ford.vehicleapi.security;

import com.ford.vehicleapi.domain.User;
import com.ford.vehicleapi.repository.UserRepository;
import com.ford.vehicleapi.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Proteção dos recursos via JWT, ponta a ponta pela cadeia de filtros do Spring Security. */
@DisplayName("Proteção de recursos com JWT")
class JwtAuthenticationTest extends IntegrationTestSupport {

    private static final String PROTECTED = "/api/v1/auth/me";

    @Autowired
    private JwtEncoder encoder;

    @Autowired
    private JwtProperties properties;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("200 com token válido")
    void acceptsValidToken() throws Exception {
        mvc.perform(get(PROTECTED).header(HttpHeaders.AUTHORIZATION, bearer(customerToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(CUSTOMER_EMAIL))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("401 sem token, com header WWW-Authenticate")
    void rejectsMissingToken() throws Exception {
        mvc.perform(get(PROTECTED))
                .andExpect(problem(401))
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"));
    }

    @Test
    @DisplayName("401 com token malformado")
    void rejectsMalformedToken() throws Exception {
        mvc.perform(get(PROTECTED).header(HttpHeaders.AUTHORIZATION, "Bearer isto.nao.e-um-jwt"))
                .andExpect(problem(401))
                .andExpect(jsonPath("$.detail").value("Token JWT inválido ou expirado"));
    }

    @Test
    @DisplayName("401 com token adulterado (assinatura não confere)")
    void rejectsTamperedToken() throws Exception {
        String token = customerToken();
        String[] parts = token.split("\\.");
        // Troca o payload por um que declara perfil ADMIN, mantendo a assinatura original.
        String forgedPayload = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"customer@ford.com\",\"uid\":3,\"roles\":[\"ADMIN\"],\"iss\":\"ford-vehicle-catalog-api\",\"exp\":9999999999}".getBytes());
        String forged = parts[0] + "." + forgedPayload + "." + parts[2];

        mvc.perform(get("/api/v1/users").header(HttpHeaders.AUTHORIZATION, bearer(forged)))
                .andExpect(problem(401));
    }

    @Test
    @DisplayName("401 com token expirado")
    void rejectsExpiredToken() throws Exception {
        User customer = userRepository.findByEmailIgnoreCase(CUSTOMER_EMAIL).orElseThrow();
        Clock twoHoursAgo = Clock.fixed(Instant.now().minus(Duration.ofHours(2)), ZoneOffset.UTC);
        String expired = new TokenService(encoder, properties, twoHoursAgo).issue(customer).value();

        mvc.perform(get(PROTECTED).header(HttpHeaders.AUTHORIZATION, bearer(expired)))
                .andExpect(problem(401))
                .andExpect(jsonPath("$.detail").value("Token JWT inválido ou expirado"));
    }

    @Test
    @DisplayName("401 com esquema de autenticação diferente de Bearer")
    void rejectsBasicAuth() throws Exception {
        mvc.perform(get(PROTECTED).header(HttpHeaders.AUTHORIZATION, "Basic YWRtaW5AZm9yZC5jb206QWRtaW5AMTIz"))
                .andExpect(problem(401));
    }

    @Test
    @DisplayName("Endpoints públicos funcionam sem token")
    void publicEndpointsDoNotRequireToken() throws Exception {
        mvc.perform(get("/api/v1/models")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }
}
