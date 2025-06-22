package fr.esgi.domain.dto.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Requête de création de colocation")
public class ColocationReqDto {
    @Schema(description = "Nom de la colocation", example = "Coloc Centre Ville")
    private String name;
    
    @Schema(description = "Ville", example = "Paris")
    private String city;
    
    @Schema(description = "Adresse complète", example = "123 Rue de la Paix")
    private String address;
    
    @Schema(description = "Code postal", example = "75001")
    private String postalCode;
}
