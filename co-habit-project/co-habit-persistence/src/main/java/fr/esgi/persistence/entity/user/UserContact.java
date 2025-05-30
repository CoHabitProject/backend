package fr.esgi.persistence.entity.user;

import fr.esgi.persistence.entity.ContactType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contacts")
@Getter
@Setter
@NoArgsConstructor
public class UserContact {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactType contactType;
    
    @Column(nullable = false)
    private String contactValue;
    
    public UserContact(ContactType contactType, String contactValue) {
        this.contactType = contactType;
        this.contactValue = contactValue;
    }
}
