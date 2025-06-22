package fr.esgi.domain.dto.space;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Schema(description = "Requête de création/modification d'un item de stock")
public class StockItemReqDto {

    @NotBlank(message = "Le nom de l'item est requis")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    @Schema(description = "Nom de l'item", example = "Lait")
    private String name;

    @NotNull(message = "La quantité est requise")
    @Min(value = 0, message = "La quantité ne peut pas être négative")
    @Schema(description = "Quantité de l'item", example = "2")
    private Integer quantity;
}
