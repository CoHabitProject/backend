package fr.esgi.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "API pour gérer l'authentification")
public class AuthStatusRest {

    @Operation(summary = "Obtenir le statut d'authentification", 
               description = "Retourne les informations sur l'utilisateur authentifié")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Utilisateur authentifié"),
        @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié")
    })
    @GetMapping("/status")
    public ResponseEntity<?> getAuthStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;

            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", true);
            response.put("username", jwtAuth.getName());
            response.put("claims", jwtAuth.getToken().getClaims());
            response.put("roles", jwtAuth.getAuthorities());

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(401).body(Map.of("authenticated", false, "message", "User is not authenticated"));
    }
}
