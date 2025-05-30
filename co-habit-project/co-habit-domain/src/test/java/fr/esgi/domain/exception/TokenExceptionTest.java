package fr.esgi.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenExceptionTest {

    @Test
    void shouldCreateTokenExceptionWithCustomValues() {
        TokenException ex = new TokenException(400, "custom_error", "Custom error description");

        assertThat(ex.getCode()).isEqualTo(400);
        assertThat(ex.getError()).isEqualTo("custom_error");
        assertThat(ex.getErrorDescription()).isEqualTo("Custom error description");
        assertThat(ex.getMessage()).isEqualTo("Custom error description");
    }

    @Test
    void shouldReturnInvalidTokenException() {
        TokenException ex = TokenException.invalidToken();

        assertThat(ex.getCode()).isEqualTo(401);
        assertThat(ex.getError()).isEqualTo("invalid_token");
        assertThat(ex.getErrorDescription()).contains("expired", "revoked", "malformed", "invalid");
    }

    @Test
    void shouldReturnExpiredTokenException() {
        TokenException ex = TokenException.expiredToken();

        assertThat(ex.getCode()).isEqualTo(401);
        assertThat(ex.getError()).isEqualTo("invalid_token");
        assertThat(ex.getErrorDescription()).contains("expired");
    }

    @Test
    void shouldReturnMalformedTokenException() {
        TokenException ex = TokenException.malformedToken();

        assertThat(ex.getCode()).isEqualTo(401);
        assertThat(ex.getError()).isEqualTo("invalid_token");
        assertThat(ex.getErrorDescription()).contains("malformed");
    }

    @Test
    void shouldReturnInsufficientScopeException() {
        String scope = "admin";
        TokenException ex = TokenException.insufficientScope(scope);

        assertThat(ex.getCode()).isEqualTo(403);
        assertThat(ex.getError()).isEqualTo("insufficient_scope");
        assertThat(ex.getErrorDescription()).contains(scope);
    }

    @Test
    void shouldReturnMissingTokenException() {
        TokenException ex = TokenException.missingToken();

        assertThat(ex.getCode()).isEqualTo(401);
        assertThat(ex.getError()).isEqualTo("invalid_request");
        assertThat(ex.getErrorDescription()).contains("missing");
    }
}
