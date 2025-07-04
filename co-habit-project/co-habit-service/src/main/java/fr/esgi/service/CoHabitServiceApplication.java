package fr.esgi.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "fr.esgi")
public class CoHabitServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoHabitServiceApplication.class, args);
    }
}
