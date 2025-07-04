package fr.esgi.domain.dto.expense;

import fr.esgi.domain.dto.user.UserProfileResDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Participant à une dépense")
public class ExpenseParticipantResDto {
    
    @Schema(description = "Identifiant unique du participant", example = "1")
    private Long id;
    
    @Schema(description = "Utilisateur participant")
    private UserProfileResDto user;
    
    @Schema(description = "Montant à payer par ce participant", example = "15.33")
    private BigDecimal shareAmount;
    
    @Schema(description = "Indique si le participant a validé le paiement", example = "false")
    private boolean validated;
    
    @Schema(description = "Date de validation du paiement", example = "2025-01-16T09:15:00")
    private String validatedAt;
    
    @Schema(description = "Indique si le créateur a confirmé avoir reçu le paiement", example = "false")
    private boolean confirmedByCreator;
    
    @Schema(description = "Date de confirmation par le créateur", example = "2025-01-16T10:30:00")
    private String confirmedByCreatorAt;
    
    @Schema(description = "Méthode de paiement utilisée", example = "Virement bancaire")
    private String paymentMethod;
    
    @Schema(description = "Indique si le paiement est entièrement réglé", example = "false")
    private boolean fullySettled;
}
