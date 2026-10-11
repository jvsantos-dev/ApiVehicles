package com.ford.vehicleapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI fordCatalogOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ford Vehicle Catalog API")
                        .version("v1")
                        .description("""
                                API REST (nível 2 de Richardson) do catálogo de veículos Ford.

                                **Como autenticar no Swagger:** execute `POST /api/v1/auth/login` com um dos usuários \
                                de exemplo, copie o `accessToken` e clique em **Authorize**.

                                | Perfil | E-mail | Senha |
                                |---|---|---|
                                | ADMIN | admin@ford.com | Admin@123 |
                                | DEALER | dealer@ford.com | Dealer@123 |
                                | CUSTOMER | customer@ford.com | Customer@123 |

                                Os endpoints de **Tabela FIPE** consomem um web service externo (API pública da FIPE) \
                                e informam o preço médio de mercado dos veículos Ford.

                                Todos os erros seguem o formato Problem Details (RFC 9457), `application/problem+json`.
                                """))
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Access token obtido em POST /api/v1/auth/login")));
    }
}
