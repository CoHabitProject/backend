package fr.esgi.rest.global;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class AppInfoRest {

    @Value("${project.version}")
    private String appVersion;

    @GetMapping("/info")
    public Map<String, String> publicInfo() {
        return Map.of(
                "message", "This is a public endpoint",
                "service", "Co-Habit Back-End",
                "version", appVersion
        );
    }
}
