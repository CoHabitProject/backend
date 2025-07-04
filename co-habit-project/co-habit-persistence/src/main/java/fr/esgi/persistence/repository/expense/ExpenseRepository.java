package fr.esgi.persistence.repository.expense;

import fr.esgi.persistence.entity.expense.Expense;
import fr.esgi.persistence.entity.space.Colocation;
import fr.esgi.persistence.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    List<Expense> findBySpace(Colocation space);
    
    List<Expense> findByPayer(User payer);
    
    List<Expense> findBySpaceAndSettled(Colocation space, boolean settled);
    
    @Query("SELECT e FROM Expense e JOIN e.participants p WHERE p.user = :user")
    List<Expense> findByParticipant(@Param("user") User user);
    
    @Query("SELECT e FROM Expense e WHERE e.space = :space AND (e.payer = :user OR EXISTS (SELECT p FROM e.participants p WHERE p.user = :user))")
    List<Expense> findBySpaceAndUserInvolved(@Param("space") Colocation space, @Param("user") User user);
    
    @Query("SELECT e FROM Expense e WHERE e.space = :space ORDER BY e.createdAt DESC")
    List<Expense> findBySpaceOrderByCreatedAtDesc(@Param("space") Colocation space);
}
