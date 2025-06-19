package fr.esgi.persistence.entity.space;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invitation_codes")
@Getter
@Setter
@NoArgsConstructor
public class InvitationCode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String code;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // Champ calculé non persisté (exemple)
    @Transient
    private boolean isExpired;
    
    // Méthode utilitaire (pas de @Transient nécessaire)
    private String generateUniqueCode() {
        return UUID.randomUUID().toString();
    }
    
    // Méthode pour calculer le champ transient
    public boolean isExpired() {
        // Logique pour déterminer si le code a expiré
        return createdAt.isBefore(LocalDateTime.now().minusDays(7));
    }
}
