package com.smarthiring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SmartHiringSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartHiringSystemApplication.class, args);
        System.out.println("===========================================");
        System.out.println("  Smart Hiring System Started Successfully!");
        System.out.println("  Server running on: http://localhost:8080");
        System.out.println("  Swagger UI: http://localhost:8080/swagger-ui/index.html");
        System.out.println("===========================================");
	}

}
