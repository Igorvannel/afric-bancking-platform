package com.afric.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway - Point d'entrée unique de la plateforme bancaire.
 * Gère le routage vers auth-service et accounting-service
 * avec validation JWT sur les routes sécurisées.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
