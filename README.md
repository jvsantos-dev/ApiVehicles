# Ford Vehicle Catalog API

API REST do catálogo de veículos Ford (modelos, versões e agendamento de test drive), com autenticação
**JWT**, três perfis de acesso, documentação **OpenAPI/Swagger** e erros padronizados em **Problem Details**.

> Sprint 3 — Arquitetura Orientada a Serviços e Web Services

## Integrantes

| Nome | RM |
|---|---|
| João Victor Oliveira dos Santos | RM557948 |
| Matheus Alcântara Estevão | RM558193 |
| Nicolle Pellegrino Jelinski | RM558610 |
| Pedro Pereira dos Santos | RM552047 |
| Eric Segawa Montagner | RM558224 |

| | |
|---|---|
| **Stack** | Java 17+, Spring Boot 4.1, Spring Security 7 (OAuth2 Resource Server / JWT), Spring Data JPA, Bean Validation, springdoc-openapi 3 |
| **Banco** | H2 em memória (padrão, zero configuração) ou PostgreSQL (profile `postgres`) |
| **Testes** | JUnit 5 + MockMvc + Spring Security Test: **73 testes** ([evidência](docs/EVIDENCIA-TESTES.md)) |
| **Arquitetura** | [docs/ARQUITETURA.md](docs/ARQUITETURA.md): diagramas, componentes, fluxos de autenticação e decisões de design |

---

## 1. Como executar

### Pré-requisitos
- **JDK 17 ou superior** (`java -version`)
- Não precisa instalar Maven: o projeto usa o Maven Wrapper (`mvnw`)

### Subir a API

```bash
# Linux / macOS / Git Bash
./mvnw spring-boot:run

# Windows (PowerShell / CMD)
mvnw.cmd spring-boot:run
```

A API sobe em **http://localhost:8080** já com usuários de exemplo e o catálogo Ford carregados.

| O quê | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI (JSON) | http://localhost:8080/v3/api-docs |

### Usuários de exemplo

| Perfil | E-mail | Senha | Pode |
|---|---|---|---|
| **ADMIN** | `admin@ford.com` | `Admin@123` | Tudo, incluindo excluir do catálogo e gerenciar perfis de usuários |
| **DEALER** | `dealer@ford.com` | `Dealer@123` | Cadastrar/editar modelos e versões; gerenciar status de test drives |
| **CUSTOMER** | `customer@ford.com` | `Customer@123` | Agendar e acompanhar os **próprios** test drives |

Novos usuários criados em `POST /api/v1/auth/register` são sempre **CUSTOMER**.

### Rodar os testes

```bash
./mvnw test          # Windows: mvnw.cmd test
```

Relatórios em `target/surefire-reports/`. O resultado da última execução está em
[docs/EVIDENCIA-TESTES.md](docs/EVIDENCIA-TESTES.md).

### Usar PostgreSQL (opcional)

```bash
# crie o banco "fordcatalog" e rode:
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

### Variáveis de ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `JWT_SECRET` | segredo de desenvolvimento | Chave HMAC do JWT (**mínimo 32 caracteres**). **Troque em produção.** |
| `JWT_EXPIRATION` | `1h` | Validade do token (`30m`, `2h`...) |
| `DB_URL` | `jdbc:postgresql://localhost:5432/fordcatalog` | Só no profile `postgres` |
| `DB_USERNAME` / `DB_PASSWORD` | `postgres` / `postgres` | Só no profile `postgres` |

---

## 2. Usando a API

### Pelo Swagger
1. Abra http://localhost:8080/swagger-ui.html
2. Execute `POST /api/v1/auth/login` com um dos usuários acima
3. Copie o `accessToken`, clique em **Authorize** e cole o token
4. Chame os endpoints protegidos

### Por linha de comando

```bash
# 1) Login -> recebe o JWT
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"customer@ford.com","password":"Customer@123"}'
```

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "expiresAt": "2026-09-27T15:02:26Z",
  "user": { "id": 3, "name": "Cliente Exemplo", "email": "customer@ford.com", "role": "CUSTOMER" }
}
```

```bash
# 2) Catálogo é público
curl http://localhost:8080/api/v1/models?category=PICKUP
curl http://localhost:8080/api/v1/models/1/versions

# 3) Recurso protegido: agendar test drive (CUSTOMER)
curl -X POST http://localhost:8080/api/v1/test-drives \
  -H "Authorization: Bearer <TOKEN>" -H "Content-Type: application/json" \
  -d '{"versionId":1,"scheduledAt":"2030-01-15T10:00:00","dealership":"Ford Centro - SP"}'
```

### Endpoints

Base: `/api/v1`

| Método | Recurso | Acesso | Sucesso | Descrição |
|---|---|---|---|---|
| POST | `/auth/register` | Público | 201 | Cadastra cliente (CUSTOMER) |
| POST | `/auth/login` | Público | 200 | Autentica e retorna JWT |
| GET | `/auth/me` | Autenticado | 200 | Dados do dono do token |
| GET | `/models?category=` | Público | 200 | Lista modelos (filtro opcional) |
| GET | `/models/{id}` | Público | 200 | Detalha modelo |
| POST | `/models` | ADMIN, DEALER | 201 | Cria modelo |
| PUT | `/models/{id}` | ADMIN, DEALER | 200 | Atualiza modelo |
| DELETE | `/models/{id}` | ADMIN | 204 | Remove modelo e versões |
| GET | `/models/{id}/versions` | Público | 200 | Lista versões do modelo |
| GET | `/models/{id}/versions/{versionId}` | Público | 200 | Detalha versão |
| POST | `/models/{id}/versions` | ADMIN, DEALER | 201 | Cria versão |
| PUT | `/models/{id}/versions/{versionId}` | ADMIN, DEALER | 200 | Atualiza versão |
| DELETE | `/models/{id}/versions/{versionId}` | ADMIN | 204 | Remove versão |
| GET | `/test-drives` | Autenticado | 200 | CUSTOMER: os seus; ADMIN/DEALER: todos |
| GET | `/test-drives/{id}` | Dono, ADMIN, DEALER | 200 | Detalha agendamento |
| POST | `/test-drives` | CUSTOMER | 201 | Agenda test drive |
| PATCH | `/test-drives/{id}/status` | ADMIN, DEALER | 200 | `REQUESTED → CONFIRMED → COMPLETED` (ou `CANCELLED`) |
| DELETE | `/test-drives/{id}` | Dono (se REQUESTED), ADMIN | 204 | Remove agendamento |
| GET | `/users` | ADMIN | 200 | Lista usuários |
| GET | `/users/{id}` | ADMIN | 200 | Detalha usuário |
| PATCH | `/users/{id}/role` | ADMIN | 200 | Altera perfil (vale a partir do próximo login) |

### O JWT

Assinado com **HS256**, validade de **1 hora** (configurável). Payload de exemplo:

```json
{
  "iss": "ford-vehicle-catalog-api",
  "sub": "customer@ford.com",
  "uid": 3,
  "name": "Cliente Exemplo",
  "roles": ["CUSTOMER"],
  "iat": 1790517746,
  "exp": 1790521346,
  "jti": "fe88c686-1dda-4a55-bf2a-44f5e707b432"
}
```

Em toda requisição protegida a API valida a **assinatura**, a **expiração** (`exp`), o **emissor** (`iss`) e a
presença de `uid`/`roles`. `roles` vira a autoridade `ROLE_<perfil>` usada nas regras de acesso, e `uid`
identifica o dono dos test drives sem consultar o banco.

### Formato de erro (padronizado)

Todos os erros, inclusive 401 e 403 gerados pela camada de segurança, seguem a
[RFC 9457 — Problem Details](https://www.rfc-editor.org/rfc/rfc9457) com `Content-Type: application/problem+json`:

```json
{
  "type": "https://api.ford-catalog.dev/errors/validation",
  "title": "Requisição inválida",
  "status": 400,
  "detail": "Um ou mais campos são inválidos",
  "instance": "/api/v1/models",
  "timestamp": "2026-09-27T14:02:26.457Z",
  "errors": [
    { "field": "name", "message": "é obrigatório" },
    { "field": "launchYear", "message": "deve ser maior ou igual a 1903 (fundação da Ford)" }
  ]
}
```

| Status | Situação |
|---|---|
| 400 | Validação, JSON malformado, parâmetro/enum inválido |
| 401 | Sem token, token inválido/expirado/adulterado, login incorreto (com `WWW-Authenticate: Bearer`) |
| 403 | Perfil sem permissão ou recurso de outro cliente |
| 404 | Recurso ou rota inexistente |
| 405 / 415 | Método ou Content-Type não suportado |
| 409 | Duplicidade ou remoção de item com dependentes |
| 422 | Regra de negócio (modelo fora de linha, transição de status inválida) |

---

## 3. Estrutura do projeto

```
src/main/java/com/ford/vehicleapi
├── config/        OpenAPI e carga inicial de dados
├── controller/    Endpoints REST (recursos, verbos, status codes)
├── domain/        Entidades JPA e enums
├── dto/           Contratos de entrada/saída (records)
├── exception/     Exceções de negócio + GlobalExceptionHandler (Problem Details)
├── repository/    Spring Data JPA
├── security/      SecurityConfig, JwtConfig, TokenService, handlers 401/403
└── service/       Regras de negócio e autorização por dono do recurso

src/test/java/com/ford/vehicleapi
├── controller/    Testes de integração por recurso (MockMvc + JWT real)
├── security/      Testes de JWT (unitário + proteção ponta a ponta)
└── support/       Base comum dos testes (login, asserção do formato de erro)
```

## 4. Atendimento aos critérios da Sprint

| Critério | Onde está |
|---|---|
| **Arquitetura** (20%) | [docs/ARQUITETURA.md](docs/ARQUITETURA.md): diagrama de componentes, modelo de dados, fluxo de login e de requisição protegida, separação em camadas |
| **Autenticação e autorização** (20%) | `security/SecurityConfig.java` (rotas públicas × protegidas, regras por perfil), `service/TestDriveService.java` (autorização por dono), 3 perfis: ADMIN, DEALER, CUSTOMER |
| **JWT** (15%) | `security/TokenService.java` (geração), `security/JwtConfig.java` (validação de assinatura, exp, iss, claims), expiração configurável, claims `uid`/`roles` usadas na autorização |
| **REST nível 2** (20%) | Controllers em `controller/`: recursos no plural, sub-recurso de versões, GET/POST/PUT/PATCH/DELETE, 201 + `Location`, 204, 400/401/403/404/405/409/415/422 |
| **Testes** (15%) | 73 testes em `src/test`: sucesso, erro de validação, 401, 403, 404, 409, 422, token expirado/adulterado. Evidência: [docs/EVIDENCIA-TESTES.md](docs/EVIDENCIA-TESTES.md) |
| **Documentação e erros** (10%) | Swagger em `/swagger-ui.html`, `exception/GlobalExceptionHandler.java` (Problem Details), este README |
