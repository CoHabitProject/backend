package fr.esgi.persistence.repository.user;

import fr.esgi.persistence.entity.user.User;
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
        User user = new User("John", "Doe");
        user.setEmail("john.doe@example.com");

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldFindByEmail() {
        User user = new User("John", "Doe");
        user.setEmail("john.doe@example.com");
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("john.doe@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get()
                            .getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldFindByFirstNameContainingIgnoreCase() {
        userRepository.save(new User("John", "Doe"));
        userRepository.save(new User("Johnny", "Smith"));
        userRepository.save(new User("Alice", "Johnson"));

        List<User> users = userRepository.findByFirstNameContainingIgnoreCase("joh");

        assertThat(users).hasSize(2);
    }
}
