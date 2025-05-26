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
@Schema(description = "Requête d'inscription utilisateur")
public class RegisterReqDto {
    @Schema(description = "Nom d'utilisateur", example = "john_doe")
    private String username;

    @Schema(description = "Nom complet", example = "John Doe")
    private String fullName;

    @Schema(description = "Prénom", example = "John")
    private String firstName;

    @Schema(description = "Nom de famille", example = "Doe")
    private String lastName;

    @Schema(description = "Date de naissance", example = "1990-01-01")
    private String birthDate;

    @Schema(description = "Genre", example = "Homme")
    private String gender;

    @Schema(description = "Numéro de téléphone", example = "+33 6 12 34 56 78")
    private String phoneNumber;

    @Schema(description = "Adresse e-mail", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Mot de passe", example = "Password123!")
    private String password;
}

