package com.afric.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Microservice d'authentification.
 * Gère l'inscription, la connexion et la gestion des utilisateurs.
 * S'enregistre automatiquement sur Eureka au démarrage.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableMongoAuditing
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
