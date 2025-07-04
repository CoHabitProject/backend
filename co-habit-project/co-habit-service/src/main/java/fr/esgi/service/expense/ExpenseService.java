package fr.esgi.service.expense;

import fr.esgi.domain.dto.expense.ExpenseReqDto;
import fr.esgi.domain.dto.expense.ExpenseResDto;
import fr.esgi.domain.dto.expense.PaymentValidationReqDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.persistence.entity.expense.Expense;
import fr.esgi.persistence.entity.expense.ExpenseParticipant;
import fr.esgi.persistence.entity.space.Colocation;
import fr.esgi.persistence.entity.user.User;
import fr.esgi.persistence.repository.expense.ExpenseParticipantRepository;
import fr.esgi.persistence.repository.expense.ExpenseRepository;
import fr.esgi.persistence.repository.space.ColocationRepository;
import fr.esgi.persistence.repository.user.UserRepository;
import fr.esgi.service.AbstractService;
import fr.esgi.service.expense.mapper.ExpenseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseService extends AbstractService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseParticipantRepository expenseParticipantRepository;
    private final ColocationRepository colocationRepository;
    private final UserRepository userRepository;
    private final ExpenseMapper expenseMapper;

    /**
     * Creates a new expense
     */
    public ExpenseResDto createExpense(ExpenseReqDto dto) throws TechnicalException {
        String userSub = getUserSub();

        User payer = userRepository.findByKeyCloakSub(userSub)
                .orElseThrow(() -> new TechnicalException(404, "Utilisateur non trouvé"));

        Colocation space = colocationRepository.findById(dto.getSpaceId())
                .orElseThrow(() -> new TechnicalException(404, "Colocation non trouvée"));

        if (!space.isRoommate(payer)) {
            throw new TechnicalException(403, "Accès refusé - Vous n'êtes pas membre de cette colocation");
        }

        Expense expense = expenseMapper.mapDtoToExpense(dto);
        expense.setPayer(payer);
        expense.setSpace(space);

        // Determine participants
        Set<User> participants = getParticipants(dto.getParticipantIds(), space);
        expense.distributeEvenly(participants);

        Expense savedExpense = expenseRepository.save(expense);
        return expenseMapper.mapExpenseToResDto(savedExpense);
    }

    /**
     * Gets all expenses for a specific colocation
     */
    @Transactional(readOnly = true)
    public List<ExpenseResDto> getExpensesBySpace(Long spaceId) throws TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                .orElseThrow(() -> new TechnicalException(404, "Utilisateur non trouvé"));

        Colocation space = colocationRepository.findById(spaceId)
                .orElseThrow(() -> new TechnicalException(404, "Colocation non trouvée"));

        if (!space.isRoommate(user)) {
            throw new TechnicalException(403, "Accès refusé - Vous n'êtes pas membre de cette colocation");
        }

        List<Expense> expenses = expenseRepository.findBySpaceOrderByCreatedAtDesc(space);
        return expenseMapper.mapExpensesToResDtos(expenses);
    }

    /**
     * Gets a specific expense by ID
     */
    @Transactional(readOnly = true)
    public ExpenseResDto getExpenseById(Long expenseId) throws TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                .orElseThrow(() -> new TechnicalException(404, "Utilisateur non trouvé"));

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new TechnicalException(404, "Dépense non trouvée"));

        if (!expense.getSpace().isRoommate(user)) {
            throw new TechnicalException(403, "Accès refusé - Vous n'êtes pas membre de cette colocation");
        }

        return expenseMapper.mapExpenseToResDto(expense);
    }

    /**
     * Gets all expenses where the user is involved (as payer or participant)
     */
    @Transactional(readOnly = true)
    public List<ExpenseResDto> getUserExpenses() throws TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                .orElseThrow(() -> new TechnicalException(404, "Utilisateur non trouvé"));

        List<Expense> payerExpenses = expenseRepository.findByPayer(user);
        List<Expense> participantExpenses = expenseRepository.findByParticipant(user);

        Set<Expense> allExpenses = new HashSet<>(payerExpenses);
        allExpenses.addAll(participantExpenses);

        return expenseMapper.mapExpensesToResDtos(allExpenses.stream().toList());
    }

    /**
     * Validates payment by a participant
     */
    public ExpenseResDto validatePayment(Long expenseId, PaymentValidationReqDto dto) throws TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                .orElseThrow(() -> new TechnicalException(404, "Utilisateur non trouvé"));

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new TechnicalException(404, "Dépense non trouvée"));

        ExpenseParticipant participant = expenseParticipantRepository.findByExpenseAndUser(expense, user)
                .orElseThrow(() -> new TechnicalException(404, "Vous n'êtes pas participant à cette dépense"));

        if (participant.isValidated()) {
            throw new TechnicalException(400, "Paiement déjà validé");
        }

        participant.validate(dto.getPaymentMethod());
        expenseParticipantRepository.save(participant);

        return expenseMapper.mapExpenseToResDto(expense);
    }

    /**
     * Confirms payment received by the expense creator
     */
    public ExpenseResDto confirmPayment(Long expenseId, Long participantUserId) throws TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                .orElseThrow(() -> new TechnicalException(404, "Utilisateur non trouvé"));

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new TechnicalException(404, "Dépense non trouvée"));

        if (!expense.getPayer().equals(user)) {
            throw new TechnicalException(403, "Seul le créateur de la dépense peut confirmer les paiements");
        }

        User participantUser = userRepository.findById(participantUserId)
                .orElseThrow(() -> new TechnicalException(404, "Utilisateur participant non trouvé"));

        expense.confirmPaymentByUser(participantUser);
        expenseRepository.save(expense);

        return expenseMapper.mapExpenseToResDto(expense);
    }

    /**
     * Confirms all payments for an expense
     */
    public ExpenseResDto confirmAllPayments(Long expenseId) throws TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                .orElseThrow(() -> new TechnicalException(404, "Utilisateur non trouvé"));

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new TechnicalException(404, "Dépense non trouvée"));

        if (!expense.getPayer().equals(user)) {
            throw new TechnicalException(403, "Seul le créateur de la dépense peut confirmer les paiements");
        }

        expense.confirmAllPayments();
        expenseRepository.save(expense);

        return expenseMapper.mapExpenseToResDto(expense);
    }

    /**
     * Deletes an expense (only creator can delete)
     */
    public void deleteExpense(Long expenseId) throws TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                .orElseThrow(() -> new TechnicalException(404, "Utilisateur non trouvé"));

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new TechnicalException(404, "Dépense non trouvée"));

        if (!expense.getPayer().equals(user)) {
            throw new TechnicalException(403, "Seul le créateur de la dépense peut la supprimer");
        }

        if (expense.isSettled()) {
            throw new TechnicalException(400, "Impossible de supprimer une dépense déjà réglée");
        }

        expenseRepository.delete(expense);
    }

    /**
     * Gets pending payments for the authenticated user
     */
    @Transactional(readOnly = true)
    public List<ExpenseResDto> getPendingPayments() throws TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                .orElseThrow(() -> new TechnicalException(404, "Utilisateur non trouvé"));

        List<ExpenseParticipant> pendingParticipants = expenseParticipantRepository.findPendingPaymentsByUser(user);
        List<Expense> pendingExpenses = pendingParticipants.stream()
                .map(ExpenseParticipant::getExpense)
                .toList();

        return expenseMapper.mapExpensesToResDtos(pendingExpenses);
    }

    /**
     * Gets pending confirmations for expenses created by the authenticated user
     */
    @Transactional(readOnly = true)
    public List<ExpenseResDto> getPendingConfirmations() throws TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                .orElseThrow(() -> new TechnicalException(404, "Utilisateur non trouvé"));

        List<ExpenseParticipant> pendingParticipants = expenseParticipantRepository.findPendingConfirmationsByPayer(user);
        List<Expense> pendingExpenses = pendingParticipants.stream()
                .map(ExpenseParticipant::getExpense)
                .distinct()
                .toList();

        return expenseMapper.mapExpensesToResDtos(pendingExpenses);
    }

    private Set<User> getParticipants(Set<Long> participantIds, Colocation space) throws TechnicalException {
        if (participantIds == null || participantIds.isEmpty()) {
            // If no specific participants, include all roommates
            return space.getRoommates();
        }

        Set<User> participants = new HashSet<>();
        for (Long participantId : participantIds) {
            User participant = userRepository.findById(participantId)
                    .orElseThrow(() -> new TechnicalException(404, "Participant non trouvé: " + participantId));
            
            if (!space.isRoommate(participant)) {
                throw new TechnicalException(400, "L'utilisateur " + participant.getEmail() + " n'est pas membre de cette colocation");
            }
            
            participants.add(participant);
        }

        return participants;
    }
}
