package fr.esgi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"fr.esgi.web", "fr.esgi.security"})
public class CoHabitApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoHabitApplication.class, args);
    }
}
