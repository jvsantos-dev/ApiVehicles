package com.ford.vehicleapi.controller;

import com.ford.vehicleapi.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("POST /api/v1/auth")
class AuthControllerTest extends IntegrationTestSupport {

    @Test
    @DisplayName("login: 200 e retorna access token Bearer com expiração")
    void loginSuccess() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "admin@ford.com", "password": "Admin@123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(not(emptyString())))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.expiresAt").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("admin@ford.com"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"))
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    @DisplayName("login: 401 com senha errada")
    void loginWrongPassword() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "admin@ford.com", "password": "errada"}
                                """))
                .andExpect(problem(401))
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos"));
    }

    @Test
    @DisplayName("login: 401 com e-mail inexistente (mesma mensagem, sem revelar usuários)")
    void loginUnknownEmail() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "ninguem@ford.com", "password": "Admin@123"}
                                """))
                .andExpect(problem(401))
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos"));
    }

    @Test
    @DisplayName("login: 400 com campos inválidos, listando os erros")
    void loginValidationError() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "nao-e-email", "password": ""}
                                """))
                .andExpect(problem(400))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("email")))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("password")));
    }

    @Test
    @DisplayName("register: 201, perfil CUSTOMER e Location do recurso criado")
    void registerSuccess() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Joana", "email": "Joana@Email.com", "password": "Senha1234"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("joana@email.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        login("joana@email.com", "Senha1234");
    }

    @Test
    @DisplayName("register: ignora tentativa de se cadastrar como ADMIN")
    void registerCannotChooseRole() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Hacker", "email": "hacker@email.com", "password": "Senha1234", "role": "ADMIN"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("register: 409 com e-mail já cadastrado")
    void registerDuplicateEmail() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Outro", "email": "CUSTOMER@ford.com", "password": "Senha1234"}
                                """))
                .andExpect(problem(409));
    }

    @Test
    @DisplayName("register: 400 com senha fraca")
    void registerWeakPassword() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Fraco", "email": "fraco@email.com", "password": "abc"}
                                """))
                .andExpect(problem(400))
                .andExpect(jsonPath("$.errors[0].field").value("password"));
    }
}
