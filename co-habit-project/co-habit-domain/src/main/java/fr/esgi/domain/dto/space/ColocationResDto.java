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

    @Schema(description = "Date de création de coloc", example = "2025-06-25T08:20:41.678Z")
    private String dateEntree;

    @Schema(description = "Date de création", example = "2025-06-25T08:20:41.678Z")
    private String createdAt;

    @Schema(description = "Date de dernière mise à jour", example = "2025-06-25T08:20:41.678Z")
    private String updatedAt;

    @Schema(description = "Code d'invitation pour rejoindre la colocation", example = "ABC12345")
    private String invitationCode;

    @Schema(description = "Manager de coloc")
    private UserProfileResDto manager;

    @Schema(description = "Liste des utilisateurs")
    private List<UserProfileResDto> users;

    public ColocationResDto(Long id, String name, String city, String address, String postalCode, int numberOfPeople) {
        super(name, city, address, postalCode);
        this.id             = id;
        this.numberOfPeople = numberOfPeople;
        this.dateEntree     = null;
        this.createdAt      = null;
        this.updatedAt      = null;
        this.invitationCode = null;
        this.manager        = null;
    }
}
