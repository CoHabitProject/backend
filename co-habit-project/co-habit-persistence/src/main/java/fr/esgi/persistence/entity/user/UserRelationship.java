package fr.esgi.persistence.entity.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "user_relationships",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {
                "parent_id",
                "child_id"
        }
    )
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRelationship {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private User child;

    // Parent confirmed the relationship ?
    @Setter
    private boolean parentConfirmed;

    // Child confirmed the relationship ?
    @Setter
    private boolean childConfirmed;

    public UserRelationship(User parent, User child) {
        this.parent = parent;
        this.child  = child;
    }

    /**
     * Fully confirmed relationship means that both parent and child
     */
    @Transient
    public boolean isFullyConfirmed() {
        return parentConfirmed && childConfirmed;
    }
}
