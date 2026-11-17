package com.pharmacy.ordonnance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableFeignClients
public class OrdonnanceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrdonnanceServiceApplication.class, args);
	}

}
