package fr.esgi.domain.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * Exception for technical/infrastructure issues
 */
@Getter
@Setter
public class TechnicalException extends Exception {
    private int code;

    public TechnicalException(int code, String message) {
        super(message);
        this.code = code;
    }

    public TechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
