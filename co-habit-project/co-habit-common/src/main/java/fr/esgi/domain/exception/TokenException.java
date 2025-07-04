package fr.esgi.domain.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * Exception for JWT token related issues (expired, malformed, etc.)
 * Following RFC 6750 guidelines for OAuth 2.0 Bearer Token errors
 */
@Getter
@Setter
public class TokenException extends TechnicalException {
    private String error;
    private String errorDescription;

    public TokenException(int code, String error, String errorDescription) {
        super(code, errorDescription);
        this.error            = error;
        this.errorDescription = errorDescription;
    }

    /**
     * Predefined token errors based on RFC 6750
     */
    public static TokenException invalidToken() {
        return new TokenException(401,
                                  "invalid_token",
                                  "The access token provided is expired, revoked, malformed, or invalid");
    }

    public static TokenException expiredToken() {
        return new TokenException(401,
                                  "invalid_token",
                                  "The access token provided has expired");
    }

    public static TokenException malformedToken() {
        return new TokenException(401, "invalid_token",
                                  "The access token provided is malformed");
    }

    public static TokenException insufficientScope(String scope) {
        return new TokenException(403,
                                  "insufficient_scope",
                                  "The request requires higher privileges than provided by the access token for scope: " + scope);
    }

    public static TokenException missingToken() {
        return new TokenException(401,
                                  "invalid_request",
                                  "The access token is missing");
    }
}
