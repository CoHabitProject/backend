package fr.esgi.service.registration;

import fr.esgi.domain.dto.auth.RegisterReqDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.domain.port.in.IUserService;
import fr.esgi.persistence.entity.user.User;
import fr.esgi.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Concrete decorator that adds database persistence functionality to the user service.
 * This decorator ensures that after a user is registered in Keycloak, their information
 * is also saved in the application database.
 */
@Service
public class DatabaseUserServiceDecorator extends AbstractUserServiceDecorator {

    private final UserRepository userRepository;

    public DatabaseUserServiceDecorator(
            IUserService keycloakRegistrationService,  // This will be autowired with KeycloakRegistrationService
            UserRepository userRepository
    ) {
        super(keycloakRegistrationService);
        this.userRepository = userRepository;
    }

    /**
     * Registers a user in Keycloak first, then persists their information in the database.
     * The method is transactional to ensure database operations are atomic.
     *
     * @param registerDto User registration data
     * @throws TechnicalException if registration fails
     */
    @Override
    @Transactional
    public void register(RegisterReqDto registerDto) throws TechnicalException {
        // First, delegate to the decorated service (Keycloak registration)
        super.register(registerDto);
        
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
        user.setBirthDate(dto.getBirthDate());
        user.setPhoneNumber(dto.getPhoneNumber());
        return user;
    }
}