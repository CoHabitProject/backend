package fr.esgi.persistence.repository;

import fr.esgi.persistence.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryInMemoryTest {

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
    void shouldFindByEmail() {
        // Given
        User user = new User("John", "Doe");
        user.setEmail("john.doe@example.com");
        userRepository.save(user);
        
        // When
        Optional<User> foundUser = userRepository.findByEmail("john.doe@example.com");
        
        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getFirstName()).isEqualTo("John");
    }
    
    @Test
    void shouldFindByFirstNameContainingIgnoreCase() {
        // Given
        userRepository.save(new User("John", "Doe"));
        userRepository.save(new User("Johnny", "Smith"));
        userRepository.save(new User("Alice", "Johnson"));
        
        // When
        List<User> users = userRepository.findByFirstNameContainingIgnoreCase("joh");
        
        // Then
        assertThat(users).hasSize(2);
    }
}
