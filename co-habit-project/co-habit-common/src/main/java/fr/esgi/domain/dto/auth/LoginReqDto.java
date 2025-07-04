package fr.esgi.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Requête de connexion utilisateur")
public class LoginReqDto {
    @Schema(description = "Nom d'utilisateur", example = "john_doe")
    private String username;
    
    @Schema(description = "Mot de passe", example = "Password123!")
    private String password;
}
