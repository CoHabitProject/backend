package fr.esgi.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "fr.esgi.rest",
        "fr.esgi.service",
        "fr.esgi.persistence",
        "fr.esgi.security"
})
public class CoHabitRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoHabitRestApplication.class, args);
    }
}
