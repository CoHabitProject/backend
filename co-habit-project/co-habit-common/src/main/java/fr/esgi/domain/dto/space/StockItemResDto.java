package fr.esgi.domain.dto.space;

import fr.esgi.domain.dto.user.UserProfileResDto;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Réponse d'un item de stock")
public class StockItemResDto extends StockItemReqDto {

    @Schema(description = "Identifiant unique de l'item", example = "1")
    private Long id;

    @Schema(description = "Utilisateur qui a ajouté l'item")
    private UserProfileResDto addedBy;

    public StockItemResDto(Long id, String name, Integer quantity) {
        super(name, quantity);
        this.id = id;
    }
}
