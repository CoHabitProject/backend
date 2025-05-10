package fr.esgi.domain.exception;

/**
 * Base exception for all domain exceptions
 */
public class FunctionalException extends RuntimeException {
    protected FunctionalException(String message) {
        super(message);
    }

    protected FunctionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
