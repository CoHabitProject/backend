package fr.esgi.rest.interne;

import fr.esgi.domain.dto.space.StockItemReqDto;
import fr.esgi.domain.dto.space.StockItemResDto;
import fr.esgi.domain.dto.space.StockReqDto;
import fr.esgi.domain.dto.space.StockResDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.service.space.StockService;
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
@RequestMapping("/api/interne/colocations/{colocationId}/stocks")
@RequiredArgsConstructor
@Tag(name = "Stock", description = "API de gestion des stocks")
public class StockRest {

    private final StockService stockService;

    @PostMapping
    @Operation(summary = "Créer un nouveau stock", description = "Crée un nouveau stock dans une colocation")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Stock créé avec succès"),
                    @ApiResponse(responseCode = "400", description = "Données invalides"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé - vous devez être membre de la colocation"),
                    @ApiResponse(responseCode = "404", description = "Colocation non trouvée"),
                    @ApiResponse(responseCode = "409", description = "Un stock avec ce titre existe déjà")
            }
    )
    public ResponseEntity<StockResDto> createStock(
            @Parameter(description = "ID de la colocation") @PathVariable Long colocationId,
            @Valid @RequestBody StockReqDto dto) throws
                                                 TechnicalException {
        StockResDto result = stockService.createStock(colocationId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(result);
    }

    @PutMapping("/{stockId}")
    @Operation(summary = "Modifier un stock", description = "Modifie un stock existant")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Stock modifié avec succès"),
                    @ApiResponse(responseCode = "400", description = "Données invalides"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé - vous devez être membre de la colocation"),
                    @ApiResponse(responseCode = "404", description = "Stock ou colocation non trouvé(e)")
            }
    )
    public ResponseEntity<StockResDto> updateStock(
            @Parameter(description = "ID de la colocation") @PathVariable Long colocationId,
            @Parameter(description = "ID du stock") @PathVariable Long stockId,
            @Valid @RequestBody StockReqDto dto) throws
                                                 TechnicalException {
        StockResDto result = stockService.updateStock(colocationId, stockId, dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @Operation(summary = "Obtenir tous les stocks", description = "Récupère tous les stocks d'une colocation")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Liste des stocks"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé - vous devez être membre de la colocation"),
                    @ApiResponse(responseCode = "404", description = "Colocation non trouvée")
            }
    )
    public ResponseEntity<List<StockResDto>> getStocksByColocation(
            @Parameter(description = "ID de la colocation") @PathVariable Long colocationId) throws
                                                                                             TechnicalException {
        List<StockResDto> result = stockService.getStocksByColocation(colocationId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{stockId}")
    @Operation(summary = "Obtenir un stock par ID", description = "Récupère un stock spécifique par son identifiant")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Stock trouvé"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé - vous devez être membre de la colocation"),
                    @ApiResponse(responseCode = "404", description = "Stock ou colocation non trouvé(e)")
            }
    )
    public ResponseEntity<StockResDto> getStockById(
            @Parameter(description = "ID de la colocation") @PathVariable Long colocationId,
            @Parameter(description = "ID du stock") @PathVariable Long stockId) throws
                                                                                TechnicalException {
        StockResDto result = stockService.getStockById(colocationId, stockId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{stockId}")
    @Operation(summary = "Supprimer un stock", description = "Supprime un stock (seul le gestionnaire peut supprimer)")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "204", description = "Stock supprimé avec succès"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé - seul le gestionnaire peut supprimer"),
                    @ApiResponse(responseCode = "404", description = "Stock ou colocation non trouvé(e)")
            }
    )
    public ResponseEntity<Void> deleteStock(
            @Parameter(description = "ID de la colocation") @PathVariable Long colocationId,
            @Parameter(description = "ID du stock") @PathVariable Long stockId) throws
                                                                                TechnicalException {
        stockService.deleteStock(colocationId, stockId);
        return ResponseEntity.noContent()
                             .build();
    }

    // Stock Items endpoints

    @PostMapping("/{stockId}/items")
    @Operation(summary = "Ajouter un item au stock", description = "Ajoute un nouvel item à un stock existant")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Item ajouté avec succès"),
                    @ApiResponse(responseCode = "400", description = "Données invalides"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé - vous devez être membre de la colocation"),
                    @ApiResponse(responseCode = "404", description = "Stock ou colocation non trouvé(e)"),
                    @ApiResponse(responseCode = "409", description = "Un item avec ce nom existe déjà dans ce stock")
            }
    )
    public ResponseEntity<StockItemResDto> addItemToStock(
            @Parameter(description = "ID de la colocation") @PathVariable Long colocationId,
            @Parameter(description = "ID du stock") @PathVariable Long stockId,
            @Valid @RequestBody StockItemReqDto dto) throws
                                                     TechnicalException {
        StockItemResDto result = stockService.addItemToStock(colocationId, stockId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(result);
    }

    @PutMapping("/{stockId}/items/{itemId}")
    @Operation(summary = "Modifier un item", description = "Modifie un item existant dans le stock")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Item modifié avec succès"),
                    @ApiResponse(responseCode = "400", description = "Données invalides"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé - vous devez être membre de la colocation"),
                    @ApiResponse(responseCode = "404", description = "Item, stock ou colocation non trouvé(e)")
            }
    )
    public ResponseEntity<StockItemResDto> updateStockItem(
            @Parameter(description = "ID de la colocation") @PathVariable Long colocationId,
            @Parameter(description = "ID du stock") @PathVariable Long stockId,
            @Parameter(description = "ID de l'item") @PathVariable Long itemId,
            @Valid @RequestBody StockItemReqDto dto) throws
                                                     TechnicalException {
        StockItemResDto result = stockService.updateStockItem(colocationId, stockId, itemId, dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{stockId}/items")
    @Operation(summary = "Obtenir tous les items", description = "Récupère tous les items d'un stock")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Liste des items"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé - vous devez être membre de la colocation"),
                    @ApiResponse(responseCode = "404", description = "Stock ou colocation non trouvé(e)")
            }
    )
    public ResponseEntity<List<StockItemResDto>> getStockItems(
            @Parameter(description = "ID de la colocation") @PathVariable Long colocationId,
            @Parameter(description = "ID du stock") @PathVariable Long stockId) throws
                                                                                TechnicalException {
        List<StockItemResDto> result = stockService.getStockItems(colocationId, stockId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{stockId}/items/{itemId}")
    @Operation(summary = "Supprimer un item", description = "Supprime un item du stock")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "204", description = "Item supprimé avec succès"),
                    @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé - vous devez être membre de la colocation"),
                    @ApiResponse(responseCode = "404", description = "Item, stock ou colocation non trouvé(e)")
            }
    )
    public ResponseEntity<Void> deleteStockItem(
            @Parameter(description = "ID de la colocation") @PathVariable Long colocationId,
            @Parameter(description = "ID du stock") @PathVariable Long stockId,
            @Parameter(description = "ID de l'item") @PathVariable Long itemId) throws
                                                                                TechnicalException {
        stockService.deleteStockItem(colocationId, stockId, itemId);
        return ResponseEntity.noContent()
                             .build();
    }
}
