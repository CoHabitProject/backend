package fr.esgi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"fr.esgi"})
public class CoHabitApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoHabitApplication.class, args);
    }
}
