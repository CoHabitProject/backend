package fr.esgi.domain.dto.space;

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
@Schema(description = "Réponse d'un stock")
public class StockResDto extends StockReqDto {

    @Schema(description = "Identifiant unique du stock", example = "1")
    private Long id;

    @Schema(description = "Nombre d'items dans le stock", example = "5")
    private Integer itemCount;

    public StockResDto(Long id, String title, String description, Integer itemCount) {
        super(title, description);
        this.id        = id;
        this.itemCount = itemCount;
    }
}
