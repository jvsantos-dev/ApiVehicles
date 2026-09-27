package com.ford.vehicleapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VehicleapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(VehicleapiApplication.class, args);
	}

}
