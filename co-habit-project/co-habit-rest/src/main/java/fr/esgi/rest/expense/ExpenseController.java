package fr.esgi.rest.expense;

import fr.esgi.domain.dto.expense.ExpenseReqDto;
import fr.esgi.domain.dto.expense.ExpenseResDto;
import fr.esgi.domain.dto.expense.PaymentValidationReqDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.service.expense.ExpenseService;
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
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Gestion des dépenses de colocation")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "Créer une nouvelle dépense")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Dépense créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Colocation non trouvée")
    })
    @PostMapping
    public ResponseEntity<ExpenseResDto> createExpense(
            @Valid @RequestBody ExpenseReqDto expenseReqDto) throws TechnicalException {
        ExpenseResDto createdExpense = expenseService.createExpense(expenseReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdExpense);
    }

    @Operation(summary = "Récupérer toutes les dépenses d'une colocation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des dépenses récupérée avec succès"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Colocation non trouvée")
    })
    @GetMapping("/space/{spaceId}")
    public ResponseEntity<List<ExpenseResDto>> getExpensesBySpace(
            @Parameter(description = "ID de la colocation") @PathVariable Long spaceId) throws TechnicalException {
        List<ExpenseResDto> expenses = expenseService.getExpensesBySpace(spaceId);
        return ResponseEntity.ok(expenses);
    }

    @Operation(summary = "Récupérer une dépense par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dépense récupérée avec succès"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Dépense non trouvée")
    })
    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseResDto> getExpenseById(
            @Parameter(description = "ID de la dépense") @PathVariable Long expenseId) throws TechnicalException {
        ExpenseResDto expense = expenseService.getExpenseById(expenseId);
        return ResponseEntity.ok(expense);
    }

    @Operation(summary = "Récupérer toutes les dépenses de l'utilisateur connecté")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des dépenses de l'utilisateur récupérée avec succès"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @GetMapping("/user")
    public ResponseEntity<List<ExpenseResDto>> getUserExpenses() throws TechnicalException {
        List<ExpenseResDto> expenses = expenseService.getUserExpenses();
        return ResponseEntity.ok(expenses);
    }

    @Operation(summary = "Valider un paiement")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paiement validé avec succès"),
            @ApiResponse(responseCode = "400", description = "Paiement déjà validé"),
            @ApiResponse(responseCode = "404", description = "Dépense ou participant non trouvé")
    })
    @PostMapping("/{expenseId}/validate-payment")
    public ResponseEntity<ExpenseResDto> validatePayment(
            @Parameter(description = "ID de la dépense") @PathVariable Long expenseId,
            @Valid @RequestBody PaymentValidationReqDto validationDto) throws TechnicalException {
        ExpenseResDto expense = expenseService.validatePayment(expenseId, validationDto);
        return ResponseEntity.ok(expense);
    }

    @Operation(summary = "Confirmer un paiement reçu")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paiement confirmé avec succès"),
            @ApiResponse(responseCode = "403", description = "Seul le créateur peut confirmer"),
            @ApiResponse(responseCode = "404", description = "Dépense ou participant non trouvé")
    })
    @PostMapping("/{expenseId}/confirm-payment/{participantUserId}")
    public ResponseEntity<ExpenseResDto> confirmPayment(
            @Parameter(description = "ID de la dépense") @PathVariable Long expenseId,
            @Parameter(description = "ID de l'utilisateur participant") @PathVariable Long participantUserId) throws TechnicalException {
        ExpenseResDto expense = expenseService.confirmPayment(expenseId, participantUserId);
        return ResponseEntity.ok(expense);
    }

    @Operation(summary = "Confirmer tous les paiements d'une dépense")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tous les paiements confirmés avec succès"),
            @ApiResponse(responseCode = "403", description = "Seul le créateur peut confirmer"),
            @ApiResponse(responseCode = "404", description = "Dépense non trouvée")
    })
    @PostMapping("/{expenseId}/confirm-all-payments")
    public ResponseEntity<ExpenseResDto> confirmAllPayments(
            @Parameter(description = "ID de la dépense") @PathVariable Long expenseId) throws TechnicalException {
        ExpenseResDto expense = expenseService.confirmAllPayments(expenseId);
        return ResponseEntity.ok(expense);
    }

    @Operation(summary = "Supprimer une dépense")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Dépense supprimée avec succès"),
            @ApiResponse(responseCode = "400", description = "Impossible de supprimer une dépense réglée"),
            @ApiResponse(responseCode = "403", description = "Seul le créateur peut supprimer"),
            @ApiResponse(responseCode = "404", description = "Dépense non trouvée")
    })
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @Parameter(description = "ID de la dépense") @PathVariable Long expenseId) throws TechnicalException {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Récupérer les paiements en attente pour l'utilisateur connecté")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des paiements en attente récupérée avec succès"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @GetMapping("/pending-payments")
    public ResponseEntity<List<ExpenseResDto>> getPendingPayments() throws TechnicalException {
        List<ExpenseResDto> pendingExpenses = expenseService.getPendingPayments();
        return ResponseEntity.ok(pendingExpenses);
    }

    @Operation(summary = "Récupérer les confirmations en attente pour l'utilisateur connecté")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des confirmations en attente récupérée avec succès"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @GetMapping("/pending-confirmations")
    public ResponseEntity<List<ExpenseResDto>> getPendingConfirmations() throws TechnicalException {
        List<ExpenseResDto> pendingExpenses = expenseService.getPendingConfirmations();
        return ResponseEntity.ok(pendingExpenses);
    }
}
