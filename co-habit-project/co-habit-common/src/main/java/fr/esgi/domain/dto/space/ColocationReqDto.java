package fr.esgi.domain.dto.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Requête de création de colocation")
public class ColocationReqDto {
    @NotBlank(message = "Le nom de la colocation est requis")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    @Schema(description = "Nom de la colocation", example = "Coloc Centre Ville")
    private String name;
    
    @NotBlank(message = "La ville est requise")
    @Size(min = 2, max = 50, message = "La ville doit contenir entre 2 et 50 caractères")
    @Schema(description = "Ville", example = "Paris")
    private String city;
    
    @NotBlank(message = "L'adresse est requise")
    @Size(min = 5, max = 200, message = "L'adresse doit contenir entre 5 et 200 caractères")
    @Schema(description = "Adresse complète", example = "123 Rue de la Paix")
    private String address;
    
    @NotBlank(message = "Le code postal est requis")
    @Size(min = 4, max = 10, message = "Le code postal doit contenir entre 4 et 10 caractères")
    @Schema(description = "Code postal", example = "75001")
    private String postalCode;
}
