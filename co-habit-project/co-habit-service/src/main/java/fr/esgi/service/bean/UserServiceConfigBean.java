package fr.esgi.service.bean;

import fr.esgi.domain.port.in.IUserService;
import fr.esgi.persistence.repository.user.UserRepository;
import fr.esgi.security.service.KeycloakRegistrationService;
import fr.esgi.service.mapper.UserMapper;
import fr.esgi.service.registration.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration class for user service wiring.
 * Ensures proper initialization order and dependency injection.
 */
@Configuration
public class UserServiceConfigBean {

    /**
     * Creates and configures the primary user service using the decorator pattern.
     * This bean is the main entry point for user registration operations.
     * 
     * @param keycloakService The Keycloak registration service
     * @param userRepository The user repository for database operations
     * @return A decorated user service that handles both Keycloak and database registration
     */
    @Bean
    @Primary
    public IUserService userServiceBean(
            KeycloakRegistrationService keycloakService,
            UserRepository userRepository,
            UserMapper userMapper
    ) {
        return new UserService(keycloakService, userRepository, userMapper);
    }
}
