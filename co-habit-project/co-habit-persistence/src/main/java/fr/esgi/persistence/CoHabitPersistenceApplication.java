package fr.esgi.persistence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "fr.esgi.persistence")
public class CoHabitPersistenceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoHabitPersistenceApplication.class, args);
    }
}
