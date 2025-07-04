package fr.esgi.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FunctionalExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        FunctionalException ex = new FunctionalException("Invalid operation");

        assertThat(ex.getMessage()).isEqualTo("Invalid operation");
    }

    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        Throwable cause = new IllegalArgumentException("Invalid param");
        FunctionalException ex = new FunctionalException("Something went wrong", cause);

        assertThat(ex.getMessage()).isEqualTo("Something went wrong");
        assertThat(ex.getCause()).isEqualTo(cause);
    }
}
