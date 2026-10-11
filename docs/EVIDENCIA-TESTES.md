# Evidência de execução dos testes

Gerado a partir dos relatórios do Maven Surefire (`target/surefire-reports`) em 11/10/2026 17:52.

Comando: `./mvnw test` (Windows: `mvnw.cmd test`)

## Resumo

| Testes | Sucesso | Falhas/Erros | Ignorados |
|---:|---:|---:|---:|
| **90** | **90** | 0 | 0 |

```
[INFO] Tests run: 90, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## Resultado por suíte

| Suíte | Testes | Falhas | Tempo (s) |
|---|---:|---:|---:|
| Contexto da aplicação | 1 | 0 | 1.27 |
| CORS | 4 | 0 | 9.71 |
| Documentação OpenAPI / Swagger | 2 | 0 | 0.99 |
| POST /api/v1/auth | 8 | 0 | 0.46 |
| /api/v1/fipe/models (web service externo) | 5 | 0 | 2.86 |
| /api/v1/test-drives | 13 | 0 | 1.99 |
| /api/v1/users (ADMIN) | 6 | 0 | 0.77 |
| /api/v1/models | 23 | 0 | 1.35 |
| /api/v1/models/{modelId}/versions | 9 | 0 | 0.65 |
| Cliente da Tabela FIPE (unitário) | 6 | 0 | 0.28 |
| Proteção de recursos com JWT | 7 | 0 | 0.21 |
| Geração e validação de JWT (unitário) | 6 | 0 | 0.02 |

## Casos de teste

### Contexto da aplicação

| | Cenário | Grupo |
|---|---|---|
| ✅ | Aplicação sobe com todas as configurações (segurança, JWT, JPA, OpenAPI) | Contexto da aplicação |

### CORS

| | Cenário | Grupo |
|---|---|---|
| ✅ | requisição real de origem autorizada recebe Access-Control-Allow-Origin | CORS |
| ✅ | preflight de origem não autorizada: 403 | CORS |
| ✅ | expõe Location ao front-end na criação de recursos (201) | CORS |
| ✅ | preflight de origem autorizada: 200 sem token, com métodos e headers liberados | CORS |

### Documentação OpenAPI / Swagger

| | Cenário | Grupo |
|---|---|---|
| ✅ | Swagger UI é acessível sem autenticação | Documentação OpenAPI / Swagger |
| ✅ | /v3/api-docs é público e documenta todos os recursos, inclusive a integração FIPE | Documentação OpenAPI / Swagger |

### POST /api/v1/auth

| | Cenário | Grupo |
|---|---|---|
| ✅ | login: 401 com senha errada | POST /api/v1/auth |
| ✅ | register: 201, perfil CUSTOMER e Location do recurso criado | POST /api/v1/auth |
| ✅ | login: 400 com campos inválidos, listando os erros | POST /api/v1/auth |
| ✅ | login: 401 com e-mail inexistente (mesma mensagem, sem revelar usuários) | POST /api/v1/auth |
| ✅ | register: ignora tentativa de se cadastrar como ADMIN | POST /api/v1/auth |
| ✅ | register: 400 com senha fraca | POST /api/v1/auth |
| ✅ | register: 409 com e-mail já cadastrado | POST /api/v1/auth |
| ✅ | login: 200 e retorna access token Bearer com expiração | POST /api/v1/auth |

### /api/v1/fipe/models (web service externo)

| | Cenário | Grupo |
|---|---|---|
| ✅ | 502 no formato Problem Details quando a FIPE está fora do ar | /api/v1/fipe/models (web service externo) |
| ✅ | 200 lista modelos sem autenticação, filtrando pelo nome | /api/v1/fipe/models (web service externo) |
| ✅ | 200 lista os anos de um modelo | /api/v1/fipe/models (web service externo) |
| ✅ | 200 devolve o preço FIPE convertido de "R$ 217.609,00" para número | /api/v1/fipe/models (web service externo) |
| ✅ | 404 quando o modelo não existe na FIPE | /api/v1/fipe/models (web service externo) |

### /api/v1/test-drives

| | Cenário | Grupo |
|---|---|---|
| ✅ | GET: CUSTOMER vê só os próprios; DEALER vê todos | /api/v1/test-drives |
| ✅ | GET /{id}: 403 quando o test drive é de outro cliente; 200 para o dono e staff | /api/v1/test-drives |
| ✅ | POST 201: CUSTOMER agenda test drive; dono vem do token, não do corpo | /api/v1/test-drives |
| ✅ | PATCH status 422 para transição inválida e 400 para status inexistente | /api/v1/test-drives |
| ✅ | DELETE 204 pelo dono enquanto REQUESTED; 422 depois de confirmado | /api/v1/test-drives |
| ✅ | POST 403: DEALER não agenda test drive (apenas clientes) | /api/v1/test-drives |
| ✅ | POST 422 para modelo fora de linha | /api/v1/test-drives |
| ✅ | POST 400 com data no passado | /api/v1/test-drives |
| ✅ | Modelo com test drive não pode ser excluído (409) | /api/v1/test-drives |
| ✅ | PATCH status: DEALER confirma e conclui; CUSTOMER recebe 403 | /api/v1/test-drives |
| ✅ | GET /{id} 404 para test drive inexistente | /api/v1/test-drives |
| ✅ | POST 404 com versão inexistente | /api/v1/test-drives |
| ✅ | POST 401 sem token | /api/v1/test-drives |

### /api/v1/users (ADMIN)

| | Cenário | Grupo |
|---|---|---|
| ✅ | GET 403 como DEALER e CUSTOMER; 401 sem token | /api/v1/users (ADMIN) |
| ✅ | Rota inexistente retorna 404 no formato padrão | /api/v1/users (ADMIN) |
| ✅ | PATCH role 422 quando o ADMIN tenta alterar o próprio perfil | /api/v1/users (ADMIN) |
| ✅ | GET 200 como ADMIN, sem expor senhas | /api/v1/users (ADMIN) |
| ✅ | GET /{id} 404 para usuário inexistente | /api/v1/users (ADMIN) |
| ✅ | PATCH role: promove cliente a DEALER; novo perfil vale no próximo token | /api/v1/users (ADMIN) |

### /api/v1/models

| | Cenário | Grupo |
|---|---|---|
| ✅ | 405 para método HTTP não suportado no recurso | /api/v1/models |
| ✅ | 400 para id com tipo inválido | GET (público) |
| ✅ | 200 filtra por categoria | GET (público) |
| ✅ | 400 com categoria inexistente | GET (público) |
| ✅ | 200 detalha um modelo com preço inicial | GET (público) |
| ✅ | 200 lista o catálogo sem autenticação | GET (público) |
| ✅ | 404 para modelo inexistente | GET (público) |
| ✅ | 403 como CUSTOMER | POST |
| ✅ | 400 com JSON malformado | POST |
| ✅ | 201 como DEALER, com Location apontando para o novo recurso | POST |
| ✅ | 409 com nome já existente | POST |
| ✅ | 400 com campos obrigatórios ausentes | POST |
| ✅ | 401 sem token | POST |
| ✅ | 415 com Content-Type não suportado | POST |
| ✅ | 201 como ADMIN | POST |
| ✅ | 204 como ADMIN e o recurso deixa de existir | DELETE |
| ✅ | 404 para modelo inexistente | DELETE |
| ✅ | 403 como DEALER (exclusão é exclusiva do ADMIN) | DELETE |
| ✅ | 401 sem token | DELETE |
| ✅ | 404 para modelo inexistente | PUT |
| ✅ | 409 ao renomear para um nome já usado | PUT |
| ✅ | 200 como DEALER atualiza o recurso por completo | PUT |
| ✅ | 403 como CUSTOMER | PUT |

### /api/v1/models/{modelId}/versions

| | Cenário | Grupo |
|---|---|---|
| ✅ | POST 403 como CUSTOMER | /api/v1/models/{modelId}/versions |
| ✅ | GET 200 lista versões ordenadas por preço, sem autenticação | /api/v1/models/{modelId}/versions |
| ✅ | PUT 200 atualiza preço da versão | /api/v1/models/{modelId}/versions |
| ✅ | POST 201 como DEALER, com Location | /api/v1/models/{modelId}/versions |
| ✅ | GET 404 quando o modelo não existe | /api/v1/models/{modelId}/versions |
| ✅ | POST 409 com versão/ano já existentes no modelo | /api/v1/models/{modelId}/versions |
| ✅ | POST 400 com preço negativo e combustível inválido | /api/v1/models/{modelId}/versions |
| ✅ | DELETE 204 como ADMIN; 403 como DEALER | /api/v1/models/{modelId}/versions |
| ✅ | GET 404 quando a versão pertence a outro modelo | /api/v1/models/{modelId}/versions |

### Cliente da Tabela FIPE (unitário)

| | Cenário | Grupo |
|---|---|---|
| ✅ | lista os anos de um modelo | Cliente da Tabela FIPE (unitário) |
| ✅ | FIPE fora do ar (503) vira ExternalServiceException | Cliente da Tabela FIPE (unitário) |
| ✅ | lista os modelos da marca Ford (código numérico vira texto) | Cliente da Tabela FIPE (unitário) |
| ✅ | modelo inexistente (FIPE responde 500 com {"error"}) vira ResourceNotFoundException | Cliente da Tabela FIPE (unitário) |
| ✅ | lê o preço com os campos em PascalCase da FIPE | Cliente da Tabela FIPE (unitário) |
| ✅ | limite de requisições (429) vira ExternalServiceException, não 404 | Cliente da Tabela FIPE (unitário) |

### Proteção de recursos com JWT

| | Cenário | Grupo |
|---|---|---|
| ✅ | 401 sem token, com header WWW-Authenticate | Proteção de recursos com JWT |
| ✅ | 401 com token adulterado (assinatura não confere) | Proteção de recursos com JWT |
| ✅ | Endpoints públicos funcionam sem token | Proteção de recursos com JWT |
| ✅ | 401 com esquema de autenticação diferente de Bearer | Proteção de recursos com JWT |
| ✅ | 200 com token válido | Proteção de recursos com JWT |
| ✅ | 401 com token malformado | Proteção de recursos com JWT |
| ✅ | 401 com token expirado | Proteção de recursos com JWT |

### Geração e validação de JWT (unitário)

| | Cenário | Grupo |
|---|---|---|
| ✅ | Token de outro emissor é rejeitado | Geração e validação de JWT (unitário) |
| ✅ | Token assinado com outra chave é rejeitado | Geração e validação de JWT (unitário) |
| ✅ | Gera token com claims de identidade, perfil, emissor e expiração | Geração e validação de JWT (unitário) |
| ✅ | Token expirado é rejeitado na validação | Geração e validação de JWT (unitário) |
| ✅ | Segredo com menos de 32 bytes impede a aplicação de subir | Geração e validação de JWT (unitário) |
| ✅ | Token não carrega dados sensíveis como a senha | Geração e validação de JWT (unitário) |
