package fr.esgi.service;

import fr.esgi.domain.exception.TechnicalException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public abstract class AbstractService {

    protected Authentication getAuthentication() throws
                                                 TechnicalException {
        Authentication authentication = SecurityContextHolder.getContext()
                                                             .getAuthentication();

        if (authentication != null && authentication.isAuthenticated())
            return authentication;
        else
            throw new TechnicalException(401, "User is not authenticated");

    }

    protected JwtAuthenticationToken getJwtAuthentication() throws
                                                          TechnicalException {
        Authentication authentication = getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth;
        } else {
            throw new TechnicalException(401, "User is not authenticated with JWT");
        }
    }

}
