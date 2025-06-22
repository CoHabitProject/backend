package fr.esgi.rest.interne;

import fr.esgi.domain.dto.space.ColocationReqDto;
import fr.esgi.domain.dto.space.ColocationResDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.service.space.ColocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colocations")
@RequiredArgsConstructor
@Tag(name = "Colocation", description = "API de gestion des colocations")
public class ColocationRest {

    private final ColocationService colocationService;

    @PostMapping
    @Operation(summary = "Créer une nouvelle colocation", description = "Crée une nouvelle colocation avec l'utilisateur authentifié comme gestionnaire")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Colocation créée avec succès"),
                    @ApiResponse(responseCode = "400", description = "Données invalides"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
            }
    )
    public ResponseEntity<ColocationResDto> createColocation(@Valid @RequestBody ColocationReqDto dto) throws
                                                                                                       TechnicalException {
        ColocationResDto result = colocationService.createColocation(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une colocation", description = "Modifie une colocation existante (seul le gestionnaire peut modifier)")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Colocation modifiée avec succès"),
                    @ApiResponse(responseCode = "400", description = "Données invalides"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé - seul le gestionnaire peut modifier"),
                    @ApiResponse(responseCode = "404", description = "Colocation non trouvée")
            }
    )
    public ResponseEntity<ColocationResDto> updateColocation(
            @Parameter(description = "ID de la colocation") @PathVariable Long id,
            @Valid @RequestBody ColocationReqDto dto) throws
                                                      TechnicalException {
        ColocationResDto result = colocationService.updateColocation(id, dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une colocation par ID", description = "Récupère les détails d'une colocation par son identifiant")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Colocation trouvée"),
                    @ApiResponse(responseCode = "404", description = "Colocation non trouvée")
            }
    )
    public ResponseEntity<ColocationResDto> getColocationById(
            @Parameter(description = "ID de la colocation") @PathVariable Long id) throws
                                                                                   TechnicalException {
        ColocationResDto result = colocationService.getColocationById(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/managed")
    @Operation(summary = "Obtenir les colocations gérées", description = "Récupère toutes les colocations gérées par l'utilisateur authentifié")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Liste des colocations gérées"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
            }
    )
    public ResponseEntity<List<ColocationResDto>> getManagedColocations() throws
                                                                          TechnicalException {
        List<ColocationResDto> result = colocationService.getManagedColocations();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my")
    @Operation(summary = "Obtenir mes colocations", description = "Récupère toutes les colocations où l'utilisateur est membre")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Liste des colocations de l'utilisateur"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
            }
    )
    public ResponseEntity<List<ColocationResDto>> getUserColocations() throws
                                                                       TechnicalException {
        List<ColocationResDto> result = colocationService.getUserColocations();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une colocation", description = "Supprime une colocation (seul le gestionnaire peut supprimer)")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "204", description = "Colocation supprimée avec succès"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé - seul le gestionnaire peut supprimer"),
                    @ApiResponse(responseCode = "404", description = "Colocation non trouvée")
            }
    )
    public ResponseEntity<Void> deleteColocation(
            @Parameter(description = "ID de la colocation") @PathVariable Long id) throws
                                                                                   TechnicalException {
        colocationService.deleteColocation(id);
        return ResponseEntity.noContent()
                             .build();
    }

    @PostMapping("/join/{invitationCode}")
    @Operation(summary = "Rejoindre une colocation", description = "Rejoint une colocation en utilisant le code d'invitation")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Colocation rejointe avec succès"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "404", description = "Code d'invitation invalide ou utilisateur non trouvé"),
                    @ApiResponse(responseCode = "409", description = "Utilisateur déjà membre ou colocation pleine")
            }
    )
    public ResponseEntity<ColocationResDto> joinColocation(
            @Parameter(description = "Code d'invitation") @PathVariable String invitationCode) throws
                                                                                               TechnicalException {
        ColocationResDto result = colocationService.joinColocation(invitationCode);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}/leave")
    @Operation(summary = "Quitter une colocation", description = "Quitte une colocation (le gestionnaire ne peut pas quitter)")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "204", description = "Colocation quittée avec succès"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "404", description = "Colocation non trouvée ou utilisateur non trouvé"),
                    @ApiResponse(responseCode = "409", description = "Utilisateur non membre ou gestionnaire ne peut pas quitter")
            }
    )
    public ResponseEntity<Void> leaveColocation(
            @Parameter(description = "ID de la colocation") @PathVariable Long id) throws
                                                                                   TechnicalException {
        colocationService.leaveColocation(id);
        return ResponseEntity.noContent()
                             .build();
    }
}
