package fr.esgi.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/interne")
public class AuthStatusRest {

    @GetMapping("/status")
    public ResponseEntity<?> getAuthStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;

            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", true);
            response.put("username", jwtAuth.getName());
            response.put("roles", jwtAuth.getAuthorities());

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(401).body(Map.of("authenticated", false, "message", "User is not authenticated"));
    }
}
