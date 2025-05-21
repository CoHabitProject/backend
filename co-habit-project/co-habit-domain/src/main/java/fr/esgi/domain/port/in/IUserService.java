package fr.esgi.domain.port.in;

import fr.esgi.domain.dto.auth.RegisterReqDto;
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
    void register(RegisterReqDto registerDto) throws TechnicalException;
}
