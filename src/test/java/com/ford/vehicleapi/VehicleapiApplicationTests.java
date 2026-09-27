package com.ford.vehicleapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("Contexto da aplicação")
class VehicleapiApplicationTests {

	@Test
	@DisplayName("Aplicação sobe com todas as configurações (segurança, JWT, JPA, OpenAPI)")
	void contextLoads() {
	}

}
