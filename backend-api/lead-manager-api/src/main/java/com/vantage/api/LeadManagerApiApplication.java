package com.vantage.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.vantage.api.repository")
public class LeadManagerApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeadManagerApiApplication.class, args);
	}

}
