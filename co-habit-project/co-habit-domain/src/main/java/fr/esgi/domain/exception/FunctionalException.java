package fr.esgi.domain.exception;

/**
 * Base exception for all domain exceptions
 */
public class FunctionalException extends RuntimeException {
    public FunctionalException(String message) {
        super(message);
    }

    public FunctionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
