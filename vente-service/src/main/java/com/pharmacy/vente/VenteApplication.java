package com.pharmacy.vente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@EnableFeignClients
@OpenAPIDefinition(info = @Info(
        title = "Vente Service API",
        version = "1.0.0",
        description = "REST API for recording pharmacy sales"))
public class VenteApplication {

    public static void main(String[] args) {
        SpringApplication.run(VenteApplication.class, args);
    }
}
