package fr.esgi.domain.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Schema(description = "Requête de création/modification de tâche")
public class TaskReqDto {
    @NotBlank(message = "Le titre de la tâche est requis")
    @Size(min = 2, max = 100, message = "Le titre doit contenir entre 2 et 100 caractères")
    @Schema(description = "Titre de la tâche", example = "Faire les courses")
    private String title;
    
    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    @Schema(description = "Description de la tâche", example = "Acheter des légumes et du pain")
    private String description;
    
    @NotNull(message = "Le statut est requis")
    @Schema(description = "Statut de la tâche", example = "PENDING")
    private TaskStatus status;
    
    @NotNull(message = "La priorité est requise")
    @Schema(description = "Priorité de la tâche", example = "HIGH")
    private TaskPriority priority;
    
    @Schema(description = "Date d'échéance", example = "2025-06-25T08:20:41.678Z")
    private String dueDate;
    
    @Schema(description = "IDs des utilisateurs assignés")
    private Set<Long> assignedUserIds;
    
    @Schema(description = "Tags associés à la tâche")
    private Set<String> tags;
}
