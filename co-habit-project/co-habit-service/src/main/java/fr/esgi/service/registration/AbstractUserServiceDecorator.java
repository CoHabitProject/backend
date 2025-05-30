package fr.esgi.service.registration;

import fr.esgi.domain.dto.auth.RegisterReqDto;
import fr.esgi.domain.dto.user.UserProfileDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.domain.port.in.IUserService;
import fr.esgi.service.AbstractService;

/**
 * Abstract decorator for user service implementations.
 * Provides the foundation for the decorator pattern used in user services.
 */
public abstract class AbstractUserServiceDecorator extends
                                                   AbstractService implements
                                                                   IUserService {

    protected final IUserService decoratedService;

    protected AbstractUserServiceDecorator(IUserService decoratedService) {
        this.decoratedService = decoratedService;
    }

    @Override
    public String register(RegisterReqDto registerDto) throws
                                                       TechnicalException {
        // The default behavior is to delegate to the decorated service
        // Subclasses will override this method to add functionality
        return decoratedService.register(registerDto);
    }

    @Override
    public UserProfileDto getUserProfile() throws
                                           TechnicalException {
        return decoratedService.getUserProfile();
    }
}
