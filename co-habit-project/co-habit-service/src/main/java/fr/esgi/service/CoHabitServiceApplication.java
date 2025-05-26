package fr.esgi.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "fr.esgi")
@EnableJpaRepositories(basePackages = "fr.esgi.persistence.repository")
@EntityScan(basePackages = "fr.esgi.persistence.entity")
public class CoHabitServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoHabitServiceApplication.class, args);
    }
}
