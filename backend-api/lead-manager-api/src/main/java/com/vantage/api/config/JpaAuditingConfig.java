package com.vantage.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA configuration separated from the main application class so that
 * {@code @WebMvcTest} slices (which only scan web-related beans) do not
 * attempt to bootstrap JPA infrastructure.
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.vantage.api.repository")
public class JpaAuditingConfig {
}
