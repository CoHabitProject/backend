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
@Schema(description = "Requête de rafraîchissement de token")
public class RefreshReqDto {
    @Schema(description = "Token de rafraîchissement", 
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;
}
