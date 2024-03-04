package io.passport.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//Gateway application
//Run this to allow access to all REST methods implemented in this project
@SpringBootApplication
public class GatewaySpring {

    public static void main(String[] args) {
        SpringApplication.run(GatewaySpring.class, args);
    }

}

