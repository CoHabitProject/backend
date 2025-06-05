package fr.esgi.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "fr.esgi.rest",
        "fr.esgi.service",
        "fr.esgi.persistence",
        "fr.esgi.security"
})
@EntityScan(basePackages = "fr.esgi.persistence.entity")
@EnableJpaRepositories(basePackages = {
        "fr.esgi.persistence.repository.user",
        "fr.esgi.persistence.repository.space"
})
public class CoHabitRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoHabitRestApplication.class, args);
    }
}
