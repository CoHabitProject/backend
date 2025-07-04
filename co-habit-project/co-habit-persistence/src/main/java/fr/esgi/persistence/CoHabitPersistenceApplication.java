package fr.esgi.persistence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"fr.esgi.persistence"})
@EntityScan(basePackages = "fr.esgi.persistence.entity")
@EnableJpaRepositories(basePackages = "fr.esgi.persistence.repository")
public class CoHabitPersistenceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoHabitPersistenceApplication.class, args);
    }
}
