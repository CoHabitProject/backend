package fr.esgi.service.registration;

import fr.esgi.domain.dto.auth.RegisterReqDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.domain.port.in.IUserService;
import fr.esgi.persistence.entity.user.User;
import fr.esgi.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private IUserService keycloakRegistrationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private RegisterReqDto dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        dto = new RegisterReqDto();
        dto.setUsername("johndoe");
        dto.setEmail("john@example.com");
        dto.setFullName("John Doe");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setGender("M");
        dto.setBirthDate("1990-01-01");
        dto.setPhoneNumber("1234567890");
    }

    @Test
    void register_ShouldSaveUserToDatabase_WhenKeycloakRegistrationSucceeds() throws TechnicalException {
        // Arrange
        String expectedKeycloakId = "keycloak-123";
        when(keycloakRegistrationService.register(dto)).thenReturn(expectedKeycloakId);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // Act
        String result = userService.register(dto);

        // Assert
        assertThat(result).isEqualTo(expectedKeycloakId);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getKeyCloakSub()).isEqualTo(expectedKeycloakId);
        assertThat(savedUser.getUsername()).isEqualTo("johndoe");
        assertThat(savedUser.getEmail()).isEqualTo("john@example.com");
        assertThat(savedUser.getFullName()).isEqualTo("John Doe");
        assertThat(savedUser.getFirstName()).isEqualTo("John");
        assertThat(savedUser.getLastName()).isEqualTo("Doe");
        assertThat(savedUser.getGender()).isEqualTo("M");
        assertThat(savedUser.getBirthDate()).isNotNull();
        assertThat(savedUser.getPhoneNumber()).isEqualTo("1234567890");
    }
}
