package fr.esgi.domain.dto.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Requête de création de dépense")
public class ExpenseReqDto {
    
    @NotBlank(message = "Le titre de la dépense est requis")
    @Size(min = 2, max = 100, message = "Le titre doit contenir entre 2 et 100 caractères")
    @Schema(description = "Titre de la dépense", example = "Courses supermarché")
    private String title;
    
    @NotBlank(message = "La description est requise")
    @Size(min = 5, max = 500, message = "La description doit contenir entre 5 et 500 caractères")
    @Schema(description = "Description de la dépense", example = "Courses pour la colocation du 15 janvier")
    private String description;
    
    @NotNull(message = "Le montant est requis")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    @Schema(description = "Montant de la dépense", example = "45.99")
    private BigDecimal amount;
    
    @NotNull(message = "L'identifiant de la colocation est requis")
    @Schema(description = "Identifiant de la colocation", example = "1")
    private Long spaceId;
    
    @Schema(description = "Identifiants des participants à la dépense (si vide, tous les colocataires participent)")
    private Set<Long> participantIds;
}
