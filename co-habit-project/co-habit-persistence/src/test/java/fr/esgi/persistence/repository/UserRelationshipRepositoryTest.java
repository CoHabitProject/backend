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
class UserRelationshipRepositoryTest {

    @Autowired
    private UserRelationshipRepository relationshipRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindConfirmedRelationships() {
        User parent = new User("Alice", "Doe");
        User child = new User("Bob", "Doe");
        userRepository.saveAll(List.of(parent, child));

        UserRelationship rel = new UserRelationship(parent, child);
        rel.setParentConfirmed(true);
        rel.setChildConfirmed(true);
        relationshipRepository.save(rel);

        List<UserRelationship> results = relationshipRepository.findAllFullyConfirmedRelationships();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getParent()).isEqualTo(parent);
        assertThat(results.get(0).getChild()).isEqualTo(child);
    }

    @Test
    void shouldFindByParentAndChild() {
        User parent = new User("John", "Smith");
        User child = new User("Eve", "Smith");
        userRepository.saveAll(List.of(parent, child));

        UserRelationship rel = new UserRelationship(parent, child);
        relationshipRepository.save(rel);

        Optional<UserRelationship> result = relationshipRepository.findByParentAndChild(parent, child);
        assertThat(result).isPresent();
    }

    @Test
    void shouldDeleteByParentAndChild() {
        User parent = new User("Alex", "Dupont");
        User child = new User("Léa", "Dupont");
        userRepository.saveAll(List.of(parent, child));

        UserRelationship rel = new UserRelationship(parent, child);
        relationshipRepository.save(rel);

        relationshipRepository.deleteByParentAndChild(parent, child);
        Optional<UserRelationship> result = relationshipRepository.findByParentAndChild(parent, child);

        assertThat(result).isEmpty();
    }
}
