package fr.esgi.persistence.repository;

import fr.esgi.persistence.entity.user.User;
import fr.esgi.persistence.entity.user.UserRelationship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("local")
public class UserRepositoryLocalDbTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRelationshipRepository relationshipRepository;

    @BeforeEach
    void setUp() {
        relationshipRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldSaveUser() {
        User user = new User("John", "Doe");
        user.setEmail("john.doe@example.com");

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldFindAllParentsWithConfirmedRelationships() {
        User parent1 = new User("Parent1", "User");
        parent1.setEmail("parent1@example.com");

        User parent2 = new User("Parent2", "User");
        parent2.setEmail("parent2@example.com");

        User child = new User("Child", "User");
        child.setEmail("child@example.com");

        userRepository.saveAll(List.of(parent1, parent2, child));

        // Relation confirmée
        UserRelationship rel1 = new UserRelationship(parent1, child);
        rel1.setParentConfirmed(true);
        rel1.setChildConfirmed(true);
        relationshipRepository.save(rel1);

        // Relation non confirmée
        UserRelationship rel2 = new UserRelationship(parent2, child);
        rel2.setParentConfirmed(false);
        rel2.setChildConfirmed(true);
        relationshipRepository.save(rel2);

        List<User> confirmedParents = userRepository.findAllParentsWithConfirmedRelationships();

        assertThat(confirmedParents).hasSize(1);
        assertThat(confirmedParents.get(0).getEmail()).isEqualTo("parent1@example.com");
    }
}
