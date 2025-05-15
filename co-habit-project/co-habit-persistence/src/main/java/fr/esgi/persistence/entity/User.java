package fr.esgi.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private Long                  id;
    @Column(unique = true)
    private String                keyCloakSub;
    @Column(unique = true)
    private String                email;
    private String                firstName;
    private String                lastName;
    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private Set<UserRelationship> children = new HashSet<>();

    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRelationship> parents = new HashSet<>();

    public User(String firstName,
                String lastName) {
        this.firstName = firstName;
        this.lastName  = lastName;
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
}
