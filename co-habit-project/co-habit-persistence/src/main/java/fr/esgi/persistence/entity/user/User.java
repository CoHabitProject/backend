package fr.esgi.persistence.entity.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue
    private Long        id;
    @Column(unique = true)
    private String        keyCloakSub;
    @Column(unique = true)
    private String        email;
    @Column(unique = true)
    private String        phoneNumber;
    private String        username;
    private String        firstName;
    private String        lastName;
    private String        fullName;
    @Column(columnDefinition = "DATE")
    private LocalDate     birthDate;
    private String        gender;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL, orphanRemoval = true
    )
    private Set<UserContact> contacts = new HashSet<>();

    @OneToMany(
            orphanRemoval = true
    )
    private Set<UserRelationship> children = new HashSet<>();

    @OneToMany(
            mappedBy = "child",
            cascade = CascadeType.ALL, orphanRemoval = true
    )
    private Set<UserRelationship> parents = new HashSet<>();

    public User(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName  = lastName;
        this.fullName  = firstName + " " + lastName;
    }

    public void addChild(User child) {
        UserRelationship rel = new UserRelationship(this, child);
        rel.setParentConfirmed(true);
        children.add(rel);
        child.getParents()
             .add(rel);
    }

    public void removeChild(User child) {
        children.removeIf(r -> r.getChild()
                                .equals(child));
        child.getParents()
             .removeIf(r -> r.getParent()
                             .equals(this));
    }

    public void addContact(UserContact contact) {
        contacts.add(contact);
        contact.setUser(this);
    }

    public void removeContact(UserContact contact) {
        contacts.remove(contact);
        contact.setUser(null);
    }
}
