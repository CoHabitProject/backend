package fr.esgi.persistence.repository;

import fr.esgi.persistence.entity.user.User;
import fr.esgi.persistence.entity.user.UserRelationship;
import fr.esgi.persistence.repository.user.UserRelationshipRepository;
import fr.esgi.persistence.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRelationshipRepository relationshipRepository;
    @Autowired
    private UserRelationshipRepository userRelationshipRepository;

    @Test
    void shouldFindUserByEmail() {
        User user = new User("Jean", "Valjean");
        user.setEmail("jean@example.com");
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("jean@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Jean");
    }

    @Test
    void shouldCheckIfEmailExists() {
        User user = new User("Marie", "Curie");
        user.setEmail("marie@example.com");
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("marie@example.com");
        assertThat(exists).isTrue();
    }

    @Test
    void shouldFindUsersByPartialName() {
        userRepository.save(new User("Lucas", "Martin"));
        userRepository.save(new User("Lucie", "Martinez"));

        List<User> users = userRepository.findByFirstNameContainingIgnoreCase("luc");
        assertThat(users).hasSize(2);

        users = userRepository.findByLastNameContainingIgnoreCase("martin");
        assertThat(users).hasSize(2);
    }

    @Test
    void shouldFindAllParentsWithConfirmedRelationships() {
        User parent = new User("Parent", "One");
        userRepository.save(parent);
        User child = new User("Child", "One");
        userRepository.save(child);

        UserRelationship relationship = new UserRelationship(parent, child);
        relationship.setParentConfirmed(true);
        relationship.setChildConfirmed(true);
        parent.getChildren().add(relationship);
        child.getParents().add(relationship);
        userRelationshipRepository.save(relationship);

        List<User> result = userRepository.findAllParentsWithConfirmedRelationships();
        assertThat(result).containsExactly(parent);
    }


    @Test
    void shouldFindAllChildrenWithConfirmedRelationships() {
        User parent = new User("Parent", "Two");
        User child = new User("Child", "Two");
        userRepository.saveAll(List.of(parent, child));

        UserRelationship rel = new UserRelationship(parent, child);
        rel.setChildConfirmed(true);
        relationshipRepository.save(rel);

        List<User> result = userRepository.findAllChildrenWithConfirmedRelationships();
        assertThat(result).containsExactly(child);
    }
}
