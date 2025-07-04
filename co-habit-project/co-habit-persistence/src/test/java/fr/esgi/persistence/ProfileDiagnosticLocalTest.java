package fr.esgi.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

@SpringBootTest()
@ActiveProfiles("local")
public class ProfileDiagnosticLocalTest {

    @Autowired
    private Environment environment;
    
    @Test
    void printActiveProfiles() {
        System.out.println("Active profiles: " + Arrays.toString(environment.getActiveProfiles()));
        System.out.println("Default profiles: " + Arrays.toString(environment.getDefaultProfiles()));
        
        String dbUrl = environment.getProperty("spring.datasource.url");
        System.out.println("Database URL: " + dbUrl);
    }
}
