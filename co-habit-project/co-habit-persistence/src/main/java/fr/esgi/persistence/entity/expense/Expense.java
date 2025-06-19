package fr.esgi.persistence.entity.expense;

import fr.esgi.persistence.entity.space.Colocation;
import fr.esgi.persistence.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
public class Expense {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime settledAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    private User payer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    private Colocation space;
    
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ExpenseParticipant> participants = new HashSet<>();
    
    @Column(nullable = false)
    private boolean settled = false;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    private void addParticipant(User user, BigDecimal share) {
        ExpenseParticipant participant = new ExpenseParticipant(this, user, share);
        participants.add(participant);
    }
    
    public void distributeEvenly(Set<User> users) {
        if (users.isEmpty()) return;
        
        BigDecimal shareAmount = amount.divide(BigDecimal.valueOf(users.size()), 2, RoundingMode.HALF_UP);
        for (User user : users) {
            addParticipant(user, shareAmount);
        }
    }
    
    // Vérifie si tous les paiements ont été confirmés par le créateur
    public boolean checkIfFullySettled() {
        if (participants.isEmpty()) {
            return false;
        }
        
        boolean allSettled = participants.stream()
                .allMatch(ExpenseParticipant::isFullySettled);
        
        // Met à jour l'état de règlement si nécessaire
        if (allSettled && !settled) {
            settled = true;
            settledAt = LocalDateTime.now();
        } else if (!allSettled && settled) {
            settled = false;
        }
        
        return allSettled;
    }
    
    // Le créateur confirme avoir reçu tous les paiements en une seule fois
    public void confirmAllPayments() {
        for (ExpenseParticipant participant : participants) {
            participant.confirmByCreator();
        }
        settled = true;
        settledAt = LocalDateTime.now();
    }
    
    // Le créateur confirme avoir reçu le paiement d'un participant spécifique
    public void confirmPaymentByUser(User user) {
        participants.stream()
                .filter(p -> p.getUser().equals(user))
                .findFirst()
                .ifPresent(ExpenseParticipant::confirmByCreator);
                
        // Vérifie si la dépense est maintenant entièrement réglée
        checkIfFullySettled();
    }
}
