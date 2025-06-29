package fr.esgi.rest.interne;

import fr.esgi.domain.dto.task.TaskReqDto;
import fr.esgi.domain.dto.task.TaskResDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.service.task.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/api/interne/collocations/{idCollocation}/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Gestion des tâches de colocation")
public class TaskRest {

    private final TaskService taskService;

    @Operation(
            summary = "Créer une nouvelle tâche",
            description = "Ajoute une nouvelle tâche à la colocation spécifiée"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Tâche créée avec succès"),
                    @ApiResponse(responseCode = "400", description = "Données de la tâche invalides"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé à cette colocation"),
                    @ApiResponse(responseCode = "404", description = "Utilisateur ou colocation non trouvé")
            }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResDto createTask(
            @Parameter(description = "ID de la colocation", required = true)
            @PathVariable Long idCollocation,
            @Parameter(description = "Données de la tâche à créer", required = true)
            @RequestBody TaskReqDto dto) throws
                                         TechnicalException {
        return taskService.createTask(idCollocation, dto);
    }

    @Operation(
            summary = "Obtenir toutes les tâches d'une colocation",
            description = "Récupère la liste de toutes les tâches de la colocation spécifiée"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Liste des tâches récupérée avec succès"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé à cette colocation"),
                    @ApiResponse(responseCode = "404", description = "Utilisateur ou colocation non trouvé")
            }
    )
    @GetMapping
    public List<TaskResDto> getTasksByColocation(
            @Parameter(description = "ID de la colocation", required = true)
            @PathVariable Long idCollocation) throws
                                              TechnicalException {
        return taskService.getTasksByColocation(idCollocation);
    }

    @Operation(
            summary = "Obtenir une tâche par son ID",
            description = "Récupère les détails d'une tâche spécifique"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Tâche récupérée avec succès"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé à cette tâche"),
                    @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
            }
    )
    @GetMapping("/{taskId}")
    public TaskResDto getTaskById(
            @Parameter(description = "ID de la colocation", required = true)
            @PathVariable Long idCollocation,
            @Parameter(description = "ID de la tâche", required = true)
            @PathVariable String taskId) throws
                                         TechnicalException {
        return taskService.getTaskById(taskId);
    }

    @Operation(
            summary = "Mettre à jour une tâche",
            description = "Modifie les informations d'une tâche existante"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Tâche mise à jour avec succès"),
                    @ApiResponse(responseCode = "400", description = "Données de la tâche invalides"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé à cette tâche"),
                    @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
            }
    )
    @PutMapping("/{taskId}")
    public TaskResDto updateTask(
            @Parameter(description = "ID de la colocation", required = true)
            @PathVariable Long idCollocation,
            @Parameter(description = "ID de la tâche", required = true)
            @PathVariable String taskId,
            @Parameter(description = "Nouvelles données de la tâche", required = true)
            @RequestBody TaskReqDto dto) throws
                                         TechnicalException {
        return taskService.updateTask(taskId, dto);
    }

    @Operation(
            summary = "Assigner une tâche à un utilisateur",
            description = "Assigne une tâche à un membre de la colocation"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Tâche assignée avec succès"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé à cette tâche"),
                    @ApiResponse(responseCode = "404", description = "Tâche ou utilisateur non trouvé")
            }
    )
    @PatchMapping("/{taskId}/assign/{userId}")
    public TaskResDto assignTask(
            @Parameter(description = "ID de la colocation", required = true)
            @PathVariable Long idCollocation,
            @Parameter(description = "ID de la tâche", required = true)
            @PathVariable String taskId,
            @Parameter(description = "ID de l'utilisateur à assigner", required = true)
            @PathVariable Long userId) throws
                                       TechnicalException {
        return taskService.assignTask(taskId, userId);
    }

    @Operation(
            summary = "Marquer une tâche comme terminée",
            description = "Change le statut d'une tâche à 'terminée'"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Tâche marquée comme terminée"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé à cette tâche"),
                    @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
            }
    )
    @PatchMapping("/{taskId}/complete")
    public TaskResDto completeTask(
            @Parameter(description = "ID de la colocation", required = true)
            @PathVariable Long idCollocation,
            @Parameter(description = "ID de la tâche", required = true)
            @PathVariable String taskId) throws
                                         TechnicalException {
        return taskService.completeTask(taskId);
    }

    @Operation(
            summary = "Supprimer une tâche",
            description = "Supprime définitivement une tâche de la colocation"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "204", description = "Tâche supprimée avec succès"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé à cette tâche"),
                    @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
            }
    )
    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(
            @Parameter(description = "ID de la colocation", required = true)
            @PathVariable Long idCollocation,
            @Parameter(description = "ID de la tâche", required = true)
            @PathVariable String taskId) throws
                                         TechnicalException {
        taskService.deleteTask(taskId);
    }

    @Operation(
            summary = "Obtenir les tâches de l'utilisateur connecté",
            description = "Récupère toutes les tâches assignées à l'utilisateur authentifié"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Tâches de l'utilisateur récupérées avec succès"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
            }
    )
    @GetMapping("/my-tasks")
    public List<TaskResDto> getUserTasks(
            @Parameter(description = "ID de la colocation", required = true)
            @PathVariable Long idCollocation) throws
                                              TechnicalException {
        return taskService.getUserTasks();
    }
}
