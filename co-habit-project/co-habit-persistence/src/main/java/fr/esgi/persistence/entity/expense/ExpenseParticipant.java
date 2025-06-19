package fr.esgi.persistence.entity.expense;

import fr.esgi.persistence.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "expense_participants")
@Getter
@Setter
@NoArgsConstructor
public class ExpenseParticipant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal shareAmount;
    
    @Column(nullable = false)
    private boolean validated = false;
    
    @Column
    private LocalDateTime validatedAt;
    
    @Column(nullable = false)
    private boolean confirmedByCreator = false;
    
    @Column
    private LocalDateTime confirmedByCreatorAt;
    
    // Méthode de paiement utilisée (optionnel)
    @Column(length = 50)
    private String paymentMethod;
    
    public ExpenseParticipant(Expense expense, User user, BigDecimal shareAmount) {
        this.expense = expense;
        this.user = user;
        this.shareAmount = shareAmount;
    }
    
    public void validate(String paymentMethod) {
        this.validated = true;
        this.validatedAt = LocalDateTime.now();
        this.paymentMethod = paymentMethod;
    }
    
    public void confirmByCreator() {
        this.confirmedByCreator = true;
        this.confirmedByCreatorAt = LocalDateTime.now();
    }
    
    public boolean isFullySettled() {
        return validated && confirmedByCreator;
    }
}
