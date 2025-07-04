package fr.esgi.domain.port.in;

import fr.esgi.domain.dto.auth.RegisterReqDto;
import fr.esgi.domain.dto.user.UserProfileResDto;
import fr.esgi.domain.exception.TechnicalException;

/**
 * Interface defining the registration service contract
 */
public interface IUserService {
    /**
     * Registers a new user in the system
     * 
     * @param registerDto User registration data transfer object
     * @throws TechnicalException if registration fails
     */
    String register(RegisterReqDto registerDto) throws TechnicalException;

    /**
     * Gets user profile by Keycloak subject ID
     * 
     * @throws TechnicalException if retrieval fails
     */
    UserProfileResDto getUserProfile() throws TechnicalException;

    /**
     * Gets user profile by Keycloak subject ID
     *
     * @param keycloakSub Keycloak subject ID
     * @throws TechnicalException if retrieval fails
     */
    UserProfileResDto getUserProfile(String keycloakSub) throws TechnicalException;
}
