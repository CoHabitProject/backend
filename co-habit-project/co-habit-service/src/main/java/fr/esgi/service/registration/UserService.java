package fr.esgi.service.registration;

import fr.esgi.domain.dto.auth.RegisterReqDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.domain.port.in.IUserService;
import fr.esgi.persistence.entity.user.User;
import fr.esgi.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Decorator for registration service that handles both Keycloak registration
 * and database persistence.
 */
@Service
public class UserService implements IUserService {

    private final IUserService   keycloakRegistrationService;
    private final UserRepository userRepository;

    public UserService(
            IUserService keycloakRegistrationService,
            UserRepository userRepository
    ) {
        this.keycloakRegistrationService = keycloakRegistrationService;
        this.userRepository              = userRepository;
    }

    @Override
    @Transactional
    public void register(RegisterReqDto registerDto) throws
                                                     TechnicalException {
        // First register the user in Keycloak
        keycloakRegistrationService.register(registerDto);

        // Then save the user details in our database
        User user = mapDtoToUser(registerDto);
        userRepository.save(user);
    }

    /**
     * Maps registration DTO to User entity
     *
     * @param dto Registration data transfer object
     * @return User entity ready to be persisted
     */
    private User mapDtoToUser(RegisterReqDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setGender(dto.getGender());
        return user;
    }
}
