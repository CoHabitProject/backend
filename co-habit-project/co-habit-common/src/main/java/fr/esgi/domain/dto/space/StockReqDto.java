package fr.esgi.domain.dto.space;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Schema(description = "Requête de création/modification d'un stock")
public class StockReqDto {

    @NotBlank(message = "Le titre est requis")
    @Size(min = 2, max = 100, message = "Le titre doit contenir entre 2 et 100 caractères")
    @Schema(description = "Titre du stock", example = "Frigo")
    private String title;

    @Schema(description = "Image du stock", example = "fridge.png")
    private String imageAsset;

    @Schema(description = "Couleur du stock", example = "#FF5733")
    private String color;

    @Min(value = 1, message = "La capacité maximale doit être d'au moins 1")
    @Max(value = 1000, message = "La capacité maximale ne peut pas dépasser 1000")
    @Schema(description = "Capacité maximale du stock", example = "50")
    private Integer maxCapacity;

    public StockReqDto(String title, String description) {
        this.title = title;
        // Note: assuming description is mapped to a different field or removed
    }
}
