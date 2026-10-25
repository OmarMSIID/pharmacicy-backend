package com.pharmacy.medicament;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "Medicament Service API",
        version = "1.0.0",
        description = "REST API for managing pharmacy medicaments"))
public class MedicamentApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicamentApplication.class, args);
    }
}
