package com.afric.accounting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Microservice comptable.
 * Gère les comptes bancaires et le journal comptable.
 * Utilise @Transactional + MongoDB Replica Set pour la cohérence des données.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableMongoAuditing
public class AccountingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountingServiceApplication.class, args);
    }
}
