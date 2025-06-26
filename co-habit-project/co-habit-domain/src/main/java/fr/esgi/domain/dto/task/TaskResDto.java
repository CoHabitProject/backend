package fr.esgi.domain.dto.task;

import fr.esgi.domain.dto.user.UserProfileResDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Réponse de tâche")
public class TaskResDto extends TaskReqDto {
    @Schema(description = "Identifiant unique de la tâche", example = "1")
    private String id;
    
    @Schema(description = "ID de l'utilisateur propriétaire")
    private Long userId;
    
    @Schema(description = "Nom de l'utilisateur propriétaire", example = "John Doe")
    private String userName;
    
    @Schema(description = "ID de la colocation")
    private Long colocationId;
    
    @Schema(description = "Nom de la colocation", example = "Coloc Centre Ville")
    private String colocationName;
    
    @Schema(description = "Date de création", example = "2025-06-25T08:20:41.678Z")
    private String createdAt;
    
    @Schema(description = "Date de completion", example = "2025-06-25T08:20:41.678Z")
    private String completedAt;
    
    @Schema(description = "ID du créateur de la tâche")
    private Long creatorId;
    
    @Schema(description = "Liste des utilisateurs assignés")
    private List<UserProfileResDto> assignedUsers;
}
