package fr.esgi.service.registration;

import fr.esgi.domain.dto.auth.RegisterReqDto;
import fr.esgi.domain.dto.user.UserProfileDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.domain.port.in.IUserService;
import fr.esgi.persistence.entity.user.User;
import fr.esgi.persistence.repository.user.UserRepository;
import fr.esgi.service.mapper.UserMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Concrete decorator that adds database persistence functionality to the user service.
 * This decorator ensures that after a user is registered in Keycloak, their information
 * is also saved in the application database.
 */
@Service
@Log4j2
public class UserService extends AbstractUserServiceDecorator {

    private final UserRepository userRepository;
    private final UserMapper     userMapper;

    public UserService(
            IUserService keycloakRegistrationService,  // This will be autowired with KeycloakRegistrationService
            UserRepository userRepository,
            UserMapper userMapper
    ) {
        super(keycloakRegistrationService);
        this.userRepository = userRepository;
        this.userMapper     = userMapper;
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
    public String register(RegisterReqDto registerDto) throws
                                                       TechnicalException {
        String userKeycloakId = super.register(registerDto);

        User user = userMapper.mapDtoToUser(userKeycloakId, registerDto);
        userRepository.save(user);
        log.info("User avec id était créer : {}", userKeycloakId);

        return userKeycloakId;
    }

    @Override
    public UserProfileDto getUserProfile() throws
                                           TechnicalException {
        String keycloakSub = super.getJwtAuthentication()
                                  .getName();  // This is the subject (sub) claim

        User user = userRepository.findByKeyCloakSub(keycloakSub)
                                  .orElseThrow(() -> new TechnicalException(404, "User not found"));
        UserProfileDto userProfileDto = userMapper.mapUserToProfileDto(user);
        return userProfileDto;
    }

    @Override
    public UserProfileDto getUserProfile(String keycloakSub) throws
                                                             TechnicalException {
        log.info("Use withouth keycloakSub");
        throw new TechnicalException(501, "Server error - This method is not implemented");
    }
}
