package fr.esgi.persistence.repository;

import fr.esgi.persistence.entity.User;
import fr.esgi.persistence.entity.UserRelationship;
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
        // Given
        User user = new User("John", "Doe");
        user.setEmail("john.doe@example.com");

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getFirstName()).isEqualTo("John");
    }
    
    @Test
    void shouldCreateParentChildRelationship() {
        // Given
        User parent = new User("Parent", "User");
        parent.setEmail("parent@example.com");
        
        User child = new User("Child", "User");
        child.setEmail("child@example.com");
        
        userRepository.save(parent);
        userRepository.save(child);
        
        // When
        parent.addChild(child);
        userRepository.save(parent);
        userRepository.save(child);
        
        // Then
        User savedParent = userRepository.findByEmail("parent@example.com").orElseThrow();
        assertThat(savedParent.getChildren()).hasSize(1);
        
        User savedChild = userRepository.findByEmail("child@example.com").orElseThrow();
        assertThat(savedChild.getParents()).hasSize(1);
    }
    
    @Test
    void shouldFindAllParentsWithConfirmedRelationships() {
        // Given
        User parent1 = new User("Parent1", "User");
        parent1.setEmail("parent1@example.com");
        
        User parent2 = new User("Parent2", "User");
        parent2.setEmail("parent2@example.com");
        
        User child = new User("Child", "User");
        child.setEmail("child@example.com");
        
        // Sauvegarder d'abord les utilisateurs
        parent1 = userRepository.save(parent1);
        parent2 = userRepository.save(parent2);
        child = userRepository.save(child);
        
        // Créer une relation confirmée pour parent1 en utilisant la méthode sûre
        parent1.addChild(child);
        userRepository.save(parent1);
        userRepository.save(child);
        
        // ===== Nouvelle approche pour créer la relation non-confirmée =====
        // Créer la relation et l'ajouter aux collections des deux côtés
        UserRelationship rel = new UserRelationship(parent2, child);
        rel.setParentConfirmed(false);
        
        // IMPORTANT: Ajouter explicitement aux collections AVANT de sauvegarder
        parent2.getChildren().add(rel);
        child.getParents().add(rel);
        
        // Sauvegarder les utilisateurs pour s'assurer que les collections sont mises à jour
        parent2 = userRepository.save(parent2);
        child = userRepository.save(child);
        
        // When
        List<User> confirmedParents = userRepository.findAllParentsWithConfirmedRelationships();
        
        // Then
        assertThat(confirmedParents).hasSize(1);
        assertThat(confirmedParents.get(0).getEmail()).isEqualTo("parent1@example.com");
    }
}
