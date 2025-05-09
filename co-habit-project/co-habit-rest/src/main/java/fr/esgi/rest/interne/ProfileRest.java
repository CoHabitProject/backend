package fr.esgi.rest.interne;

import fr.esgi.rest.dto.user.UserProfileDto;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interne")
@Tag(name = "Authentication", description = "API pour gérer l'authentification")
public class ProfileRest {

    @Operation(
            summary = "Obtenir le statut d'authentification",
            description = "Retourne les informations sur l'utilisateur authentifié"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Utilisateur authentifié"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié")
            }
    )
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext()
                                                             .getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            Map<String, Object>    claims  = jwtAuth.getToken()
                                                    .getClaims();

            // Extraction des rôles
            List<String> roles = new ArrayList<>();
            if (claims.containsKey("realm_access")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
                if (realmAccess.containsKey("roles")) {
                    @SuppressWarnings("unchecked")
                    List<String> realmRoles = (List<String>) realmAccess.get("roles");
                    roles.addAll(realmRoles);
                }
            }

            // Construction du DTO avec les informations du JWT
            UserProfileDto userProfile = UserProfileDto.builder()
                                                       .id((String) claims.get("sub"))
                                                       .username((String) claims.get("preferred_username"))
                                                       .email((String) claims.get("email"))
                                                       .emailVerified(claims.containsKey("email_verified") ? (Boolean) claims.get("email_verified") : false)
                                                       .roles(roles)
                                                       .build();

            return ResponseEntity.ok(userProfile);
        }

        return ResponseEntity.status(401)
                             .body(Map.of("authenticated", false, "message", "User is not authenticated"));
    }
}
