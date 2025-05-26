package fr.esgi.service.registration;

import fr.esgi.domain.dto.auth.RegisterReqDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.domain.port.in.IUserService;

/**
 * Abstract decorator for the user service.
 * Implements the decorator pattern by wrapping an IUserService instance.
 */
public abstract class AbstractUserServiceDecorator implements IUserService {
    
    protected final IUserService decoratedService;
    
    protected AbstractUserServiceDecorator(IUserService decoratedService) {
        this.decoratedService = decoratedService;
    }
    
    @Override
    public String register(RegisterReqDto registerDto) throws TechnicalException {
        // The default behavior is to delegate to the decorated service
        // Subclasses will override this method to add functionality
        return decoratedService.register(registerDto);
    }
}
