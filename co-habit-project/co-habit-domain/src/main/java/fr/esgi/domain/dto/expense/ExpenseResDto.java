package fr.esgi.domain.dto.expense;

import fr.esgi.domain.dto.user.UserProfileResDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Réponse de dépense")
public class ExpenseResDto {
    
    @Schema(description = "Identifiant unique de la dépense", example = "1")
    private Long id;
    
    @Schema(description = "Titre de la dépense", example = "Courses supermarché")
    private String title;
    
    @Schema(description = "Description de la dépense", example = "Courses pour la colocation du 15 janvier")
    private String description;
    
    @Schema(description = "Montant total de la dépense", example = "45.99")
    private BigDecimal amount;
    
    @Schema(description = "Date de création", example = "2025-01-15T10:30:00")
    private String createdAt;
    
    @Schema(description = "Date de règlement", example = "2025-01-20T14:30:00")
    private String settledAt;
    
    @Schema(description = "Indique si la dépense est réglée", example = "false")
    private boolean settled;
    
    @Schema(description = "Utilisateur qui a payé la dépense")
    private UserProfileResDto payer;
    
    @Schema(description = "Identifiant de la colocation", example = "1")
    private Long spaceId;
    
    @Schema(description = "Nom de la colocation", example = "Coloc Centre Ville")
    private String spaceName;
    
    @Schema(description = "Liste des participants à la dépense")
    private List<ExpenseParticipantResDto> participants;
}
