package fr.esgi.rest.global;

import fr.esgi.domain.dto.auth.LoginReqDto;
import fr.esgi.domain.dto.auth.RefreshReqDto;
import fr.esgi.domain.dto.auth.RegisterReqDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.domain.port.in.IUserService;
import fr.esgi.security.service.KeycloakAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthRestTest {

    @Mock
    private IUserService regService;

    @Mock
    private KeycloakAuthService authService;

    @InjectMocks
    private AuthRest authRest;

    private RegisterReqDto registerReqDto;
    private LoginReqDto loginReqDto;
    private RefreshReqDto refreshReqDto;
    private Map<String, Object> tokens;

    @BeforeEach
    void setUp() {
        // Setup RegisterReqDto
        registerReqDto = new RegisterReqDto();
        registerReqDto.setUsername("testuser");
        registerReqDto.setEmail("test@example.com");
        registerReqDto.setPassword("password123");

        // Setup LoginReqDto
        loginReqDto = new LoginReqDto();
        loginReqDto.setUsername("testuser");
        loginReqDto.setPassword("password123");

        // Setup RefreshReqDto
        refreshReqDto = new RefreshReqDto();
        refreshReqDto.setRefreshToken("refresh-token");

        // Setup tokens
        tokens = new HashMap<>();
        tokens.put("access_token", "access-token");
        tokens.put("refresh_token", "refresh-token");
    }

    @Test
    void register_ShouldReturn201_WhenRegistrationSuccessful() throws TechnicalException {
        // Act
        ResponseEntity<Void> response = authRest.register(registerReqDto);

        // Assert
        assert response.getStatusCodeValue() == 201;
        verify(regService, times(1)).register(registerReqDto);
    }


    @Test
    void register_ShouldThrowTechnicalException_WhenRegistrationFails() throws TechnicalException {
        // Arrange
        doThrow(new TechnicalException(500, "Registration failed"))
            .when(regService).register(any(RegisterReqDto.class));

        // Act & Assert
        try {
            authRest.register(registerReqDto);
        } catch (TechnicalException e) {
            assert e.getMessage().equals("Registration failed");
        }
        verify(regService, times(1)).register(registerReqDto);
    }

    @Test
    void login_ShouldReturn200WithTokens_WhenLoginSuccessful() {
        // Arrange
        when(authService.login(anyString(), anyString()))
            .thenReturn(Mono.just(tokens));

        // Act
        Mono<ResponseEntity<Map<String, Object>>> responseMono = authRest.login(loginReqDto);

        // Assert
        StepVerifier.create(responseMono)
            .expectNextMatches(response ->
                response.getStatusCodeValue() == 200 &&
                response.getBody().equals(tokens))
            .verifyComplete();

        verify(authService, times(1)).login(loginReqDto.getUsername(), loginReqDto.getPassword());
    }

    @Test
    void login_ShouldReturn401_WhenLoginFails() {
        // Arrange
        when(authService.login(anyString(), anyString()))
            .thenReturn(Mono.error(new RuntimeException("Invalid credentials")));

        // Act
        Mono<ResponseEntity<Map<String, Object>>> responseMono = authRest.login(loginReqDto);

        // Assert
        StepVerifier.create(responseMono)
            .expectError(RuntimeException.class)
            .verify();

        verify(authService, times(1)).login(loginReqDto.getUsername(), loginReqDto.getPassword());
    }

    @Test
    void refresh_ShouldReturn200WithNewTokens_WhenRefreshSuccessful() {
        // Arrange
        when(authService.refresh(anyString()))
            .thenReturn(Mono.just(tokens));

        // Act
        Mono<ResponseEntity<Map<String, Object>>> responseMono = authRest.refresh(refreshReqDto);

        // Assert
        StepVerifier.create(responseMono)
            .expectNextMatches(response ->
                response.getStatusCodeValue() == 200 &&
                response.getBody().equals(tokens))
            .verifyComplete();

        verify(authService, times(1)).refresh(refreshReqDto.getRefreshToken());
    }

    @Test
    void refresh_ShouldReturn401_WhenRefreshFails() {
        // Arrange
        when(authService.refresh(anyString()))
            .thenReturn(Mono.error(new RuntimeException("Invalid refresh token")));

        // Act
        Mono<ResponseEntity<Map<String, Object>>> responseMono = authRest.refresh(refreshReqDto);

        // Assert
        StepVerifier.create(responseMono)
            .expectError(RuntimeException.class)
            .verify();

        verify(authService, times(1)).refresh(refreshReqDto.getRefreshToken());
    }
}
