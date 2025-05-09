package fr.esgi.rest.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Requête d'inscription utilisateur")
public class RegisterReqDto {
    @Schema(description = "Nom d'utilisateur", example = "john_doe")
    private String username;
    
    @Schema(description = "Adresse e-mail", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "Mot de passe", example = "Password123!")
    private String password;
}

