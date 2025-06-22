package fr.esgi.domain.dto.space;

import fr.esgi.domain.dto.user.UserProfileResDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Réponse de colocation")
public class ColocationResDto extends ColocationReqDto {
    @Schema(description = "Identifiant unique de la colocation", example = "1")
    private Long id;
    
    @Schema(description = "Nombre de personnes", example = "4")
    private int numberOfPeople;

    @Schema(description = "Date de création de coloc")
    private String dateEntree;
    
    @Schema(description = "Manager de coloc")
    private UserProfileResDto manager;
    
    @Schema(description = "Liste des utilisateurs")
    private List<UserProfileResDto> users;

    public ColocationResDto(Long id, String name, String city, String address, String postalCode, int numberOfPeople) {
        super(name, city, address, postalCode);
        this.id = id;
        this.numberOfPeople = numberOfPeople;
        this.dateEntree = null;
        this.manager = null;
    }
}
