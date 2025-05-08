package fr.esgi.web.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/hello")
    // Enlève temporairement l'annotation
    // @PreAuthorize("hasAuthority('SCOPE_profile')")
    public String hello(@AuthenticationPrincipal(errorOnInvalidType = false) Jwt jwt) {

        if (jwt == null) {
            return "JWT est null - pas d'authentification";
        }
        System.out.println(jwt.getClaims());

        StringBuilder sb = new StringBuilder();
        sb.append("Hello ").append(jwt.getClaimAsString("preferred_username")).append("\n");
        sb.append("Claims: \n");

        jwt.getClaims().forEach((key, value) ->
                sb.append(key).append(": ").append(value).append("\n")
        );

        return sb.toString();
    }

    @GetMapping("/public")
    public Map<String, String> publicEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Cet endpoint est accessible sans authentification");
        return response;
    }

    @GetMapping("/customers")
    public Map<String, Object> getCustomerInfo(@AuthenticationPrincipal(errorOnInvalidType = false) Jwt jwt) {
        System.out.println(">>> /api/customers hit");
        Map<String, Object> response = new HashMap<>();

        if (jwt == null) {
            System.out.println(">>> JWT is null");
            response.put("error", "Non authentifié");
            return response;
        }

        System.out.println(">>> JWT received: " + jwt.getClaims());

        response.put("username", jwt.getClaimAsString("preferred_username"));
        response.put("email", jwt.getClaimAsString("email"));
        response.put("name", jwt.getClaimAsString("name"));
        response.put("roles", jwt.getClaimAsString("realm_access.roles"));

        return response;
    }

}
