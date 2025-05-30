package fr.esgi.persistence.repository.user;

import fr.esgi.persistence.entity.user.User;
import fr.esgi.persistence.entity.user.UserRelationship;
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

    @Test
    void shouldSaveUser() {
        User user = new User("John", "Doe");
        user.setEmail("john.doe@example.com");

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldCreateParentChildRelationship() {
        User parent = new User("Parent", "User");
        parent.setEmail("parent@example.com");

        User child = new User("Child", "User");
        child.setEmail("child@example.com");

        userRepository.save(parent);
        userRepository.save(child);

        parent.addChild(child);
        userRepository.save(parent);
        userRepository.save(child);

        User savedParent = userRepository.findByEmail("parent@example.com")
                                         .orElseThrow();
        assertThat(savedParent.getChildren()).hasSize(1);

        User savedChild = userRepository.findByEmail("child@example.com")
                                        .orElseThrow();
        assertThat(savedChild.getParents()).hasSize(1);
    }

    @Test
    void shouldFindAllParentsWithConfirmedRelationships() {
        User parent1 = new User("Parent1", "User");
        parent1.setEmail("parent1@example.com");

        User parent2 = new User("Parent2", "User");
        parent2.setEmail("parent2@example.com");

        User child = new User("Child", "User");
        child.setEmail("child@example.com");

        // Save users first
        parent1 = userRepository.save(parent1);
        parent2 = userRepository.save(parent2);
        child   = userRepository.save(child);

        // Create confirmed relationship for parent1
        parent1.addChild(child);
        userRepository.save(parent1);
        userRepository.save(child);

        // Create unconfirmed relationship for parent2
        UserRelationship rel = new UserRelationship(parent2, child);
        rel.setParentConfirmed(false);

        // IMPORTANT: Add explicitly to collections BEFORE saving
        parent2.getChildren()
               .add(rel);
        child.getParents()
             .add(rel);

        parent2 = userRepository.save(parent2);
        child   = userRepository.save(child);

        List<User> confirmedParents = userRepository.findAllParentsWithConfirmedRelationships();

        assertThat(confirmedParents).hasSize(1);
        assertThat(confirmedParents.get(0)
                                   .getEmail()).isEqualTo("parent1@example.com");
    }
}
