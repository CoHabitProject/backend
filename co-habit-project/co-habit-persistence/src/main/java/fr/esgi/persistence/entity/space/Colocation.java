package fr.esgi.persistence.entity.space;

import fr.esgi.persistence.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "colocations")
@Getter
@Setter
@NoArgsConstructor
public class Colocation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private String address;
    
    private String city;
    
    private String postalCode;
    
    private String country;
    
    @Column(name = "max_roommates")
    private Integer maxRoommates;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Gestionnaire de la colocation (propriétaire/administrateur)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;
    
    // Liste des colocataires (relation Many-to-Many)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "colocation_roommates",
        joinColumns = @JoinColumn(name = "colocation_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> roommates = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Colocation(String name, String address, User manager) {
        this.name = name;
        this.address = address;
        this.manager = manager;
    }
    
    // Méthodes utilitaires pour gérer les colocataires
    public void addRoommate(User user) {
        roommates.add(user);
    }
    
    public void removeRoommate(User user) {
        roommates.remove(user);
    }
    
    public boolean isManager(User user) {
        return manager != null && manager.equals(user);
    }
    
    public boolean isRoommate(User user) {
        return roommates.contains(user);
    }
    
    public boolean isFull() {
        return maxRoommates != null && roommates.size() >= maxRoommates;
    }
    
    public int getCurrentRoommatesCount() {
        return roommates.size();
    }
}