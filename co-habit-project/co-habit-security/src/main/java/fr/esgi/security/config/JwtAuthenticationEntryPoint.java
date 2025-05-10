package fr.esgi.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.esgi.domain.dto.error.ErrorResponseDto;
import fr.esgi.domain.exception.TokenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom entry point to handle JWT authentication errors following RFC 6750
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        
        // Convertir l'AuthenticationException en TokenException
        TokenException tokenException = convertToTokenException(authException);

        // Configurer la réponse HTTP
        response.setStatus(tokenException.getCode());
        response.setContentType("application/json;charset=UTF-8");
        
        // Ajouter l'en-tête WWW-Authenticate selon RFC 6750
        response.setHeader("WWW-Authenticate",
                "Bearer realm=\"api\", error=\"" + tokenException.getError() + 
                "\", error_description=\"" + tokenException.getErrorDescription() + "\"");

        // Créer la réponse DTO
        ErrorResponseDto errorResponseDto = ErrorResponseDto.builder()
                .status(tokenException.getCode())
                .error(tokenException.getError())
                .message(tokenException.getErrorDescription())
                .path(request.getRequestURI())
                .build();

        // Écrire les détails d'erreur dans la réponse
        objectMapper.writeValue(response.getOutputStream(), errorResponseDto);
    }

    /**
     * Convertit une AuthenticationException en TokenException appropriée
     */
    private TokenException convertToTokenException(AuthenticationException authException) {
        if (authException instanceof InvalidBearerTokenException) {
            String message = authException.getMessage();
            if (message != null) {
                if (message.contains("expired")) {
                    return TokenException.expiredToken();
                } else if (message.contains("malformed")) {
                    return TokenException.malformedToken();
                }
            }
            return TokenException.invalidToken();
        } else if (authException.getMessage() != null &&
                   authException.getMessage().contains("insufficient_scope")) {
            return TokenException.insufficientScope("requested resource");
        } else {
            return TokenException.invalidToken();
        }
    }
}
