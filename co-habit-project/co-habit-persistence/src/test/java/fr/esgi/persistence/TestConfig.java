package fr.esgi.persistence;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EnableAutoConfiguration
@EntityScan("fr.esgi.persistence.entity")
@EnableJpaRepositories("fr.esgi.persistence.repository")
@ComponentScan("fr.esgi.persistence")
public class TestConfig {
    // This is empty on purpose
}
