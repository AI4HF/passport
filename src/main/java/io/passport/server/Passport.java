package io.passport.server;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Passport application
 * Run this to allow access to all REST methods implemented in this project
 */
@SpringBootApplication
@OpenAPIDefinition
public class Passport {

    public static void main(String[] args) {
        SpringApplication.run(Passport.class, args);
    }

}