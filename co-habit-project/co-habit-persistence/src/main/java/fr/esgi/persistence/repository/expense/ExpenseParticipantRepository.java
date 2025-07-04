package fr.esgi.persistence.repository.expense;

import fr.esgi.persistence.entity.expense.Expense;
import fr.esgi.persistence.entity.expense.ExpenseParticipant;
import fr.esgi.persistence.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseParticipantRepository extends JpaRepository<ExpenseParticipant, Long> {
    
    List<ExpenseParticipant> findByExpense(Expense expense);
    
    List<ExpenseParticipant> findByUser(User user);
    
    Optional<ExpenseParticipant> findByExpenseAndUser(Expense expense, User user);
    
    List<ExpenseParticipant> findByUserAndValidated(User user, boolean validated);
    
    @Query("SELECT ep FROM ExpenseParticipant ep WHERE ep.user = :user AND ep.validated = false")
    List<ExpenseParticipant> findPendingPaymentsByUser(@Param("user") User user);
    
    @Query("SELECT ep FROM ExpenseParticipant ep WHERE ep.expense.payer = :payer AND ep.confirmedByCreator = false")
    List<ExpenseParticipant> findPendingConfirmationsByPayer(@Param("payer") User payer);
}
