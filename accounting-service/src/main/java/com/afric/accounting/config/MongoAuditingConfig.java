package com.afric.accounting.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * Audit MongoDB pour l'Accounting Service.
 * L'identité vient du header X-User-Id propagé par le Gateway.
 * Ici on retourne SYSTEM par défaut (le contexte HTTP n'est pas disponible dans AuditorAware).
 * Pour un audit précis, injecter le RequestContextHolder.
 */
@Configuration
public class MongoAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("SYSTEM");
    }
}
