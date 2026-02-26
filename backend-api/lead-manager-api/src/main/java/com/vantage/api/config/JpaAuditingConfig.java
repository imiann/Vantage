package com.vantage.api.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA configuration separated from the main application class.
 * <p>
 * {@code @ConditionalOnBean(EntityManagerFactory.class)} ensures this config
 * only activates when an EntityManagerFactory is present (i.e., full context
 * or @DataJpaTest), preventing it from crashing @WebMvcTest slices.
 */
@Configuration
@ConditionalOnBean(EntityManagerFactory.class)
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.vantage.api.repository")
public class JpaAuditingConfig {
}
