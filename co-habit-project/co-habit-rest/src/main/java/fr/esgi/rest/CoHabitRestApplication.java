package fr.esgi.rest;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"fr.esgi.rest", "fr.esgi.service"})
public class CoHabitRestApplication {
}
