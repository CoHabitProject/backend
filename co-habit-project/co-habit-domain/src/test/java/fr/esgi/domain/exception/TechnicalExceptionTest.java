package fr.esgi.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class TechnicalExceptionTest {

    @Test
    void shouldCreateExceptionWithCodeAndMessage() {
        TechnicalException ex = new TechnicalException(500, "Internal error");

        assertThat(ex.getMessage()).isEqualTo("Internal error");
        assertThat(ex.getCode()).isEqualTo(500);
    }

    @Test
    void shouldCreateExceptionWithCause() {
        Throwable cause = new RuntimeException("Root cause");
        TechnicalException ex = new TechnicalException("Something failed", cause);

        assertThat(ex.getMessage()).isEqualTo("Something failed");
        assertThat(ex.getCause()).isEqualTo(cause);
    }
}
