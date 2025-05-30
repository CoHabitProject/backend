package fr.esgi.rest.interne;

import fr.esgi.domain.dto.user.UserProfileDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.domain.port.in.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interne")
@Tag(name = "Profile", description = "API pour gérer le profil utilisateur")
public class ProfileRest {

    private final IUserService userService;

    @Autowired
    public ProfileRest(IUserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Obtenir le profil utilisateur",
            description = "Retourne les informations complètes sur l'utilisateur authentifié"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Profil utilisateur récupéré"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
            }
    )
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getUserProfile() throws
                                                           TechnicalException {
        return ResponseEntity.ok(userService.getUserProfile());
    }
}
