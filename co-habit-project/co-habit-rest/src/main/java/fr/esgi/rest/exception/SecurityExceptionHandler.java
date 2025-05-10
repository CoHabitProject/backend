package fr.esgi.rest.exception;

import fr.esgi.domain.dto.error.ErrorResponseDto;
import fr.esgi.domain.exception.FunctionalException;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.domain.exception.TokenException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ErrorResponseDto> handleTokenException(TokenException ex, WebRequest request) {
        ErrorResponseDto errorResponseDto = ErrorResponseDto.builder()
                .status(ex.getCode())
                .error(ex.getError())
                .message(ex.getErrorDescription())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("WWW-Authenticate", 
                String.format("Bearer realm=\"api\", error=\"%s\", error_description=\"%s\"", 
                        ex.getError(), ex.getErrorDescription()));
        
        return new ResponseEntity<>(errorResponseDto, headers, HttpStatus.valueOf(ex.getCode()));
    }
    
    @ExceptionHandler(TechnicalException.class)
    public ResponseEntity<ErrorResponseDto> handleTechnicalException(TechnicalException ex, WebRequest request) {
        ErrorResponseDto errorResponseDto = ErrorResponseDto.builder()
                .status(ex.getCode())
                .error("technical_error")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return new ResponseEntity<>(errorResponseDto, HttpStatus.valueOf(ex.getCode()));
    }
    
    @ExceptionHandler(FunctionalException.class)
    public ResponseEntity<ErrorResponseDto> handleFunctionalException(FunctionalException ex, WebRequest request) {
        ErrorResponseDto errorResponseDto = ErrorResponseDto.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("functional_error")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(InvalidBearerTokenException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidBearerTokenException(InvalidBearerTokenException ex, WebRequest request) {
        TokenException tokenException;
        
        if (ex.getMessage().contains("expired")) {
            tokenException = TokenException.expiredToken();
        } else if (ex.getMessage().contains("malformed")) {
            tokenException = TokenException.malformedToken();
        } else {
            tokenException = TokenException.invalidToken();
        }
        
        return handleTokenException(tokenException, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        TokenException tokenException = TokenException.insufficientScope("requested resource");
        return handleTokenException(tokenException, request);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex, WebRequest request) {
        ErrorResponseDto errorResponseDto = ErrorResponseDto.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("internal_server_error")
                .message("Une erreur inattendue s'est produite: " + ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return new ResponseEntity<>(errorResponseDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
