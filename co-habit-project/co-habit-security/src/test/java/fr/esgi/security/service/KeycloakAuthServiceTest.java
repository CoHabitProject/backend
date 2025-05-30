package fr.esgi.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class KeycloakAuthServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec uriSpec;

    @Mock
    private WebClient.RequestBodySpec bodySpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> headersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private KeycloakAuthService keycloakAuthService;

    private final String tokenUrl = "http://localhost/realms/myrealm/protocol/openid-connect/token";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(webClientBuilder.build()).thenReturn(webClient);
        keycloakAuthService = new KeycloakAuthService(
                webClient,
                "http://localhost/realms/myrealm/protocol/openid-connect/token",
                "my-client",
                "my-secret"
        );
    }

    @Test
    void login_ShouldReturnTokens_WhenSuccessful() {
        // Given
        Map<String, Object> expectedTokens = Map.of("access_token", "token123");

        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(tokenUrl)).thenReturn(bodySpec);
        when(bodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(bodySpec);
        when(bodySpec.body(any(BodyInserters.FormInserter.class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(expectedTokens));

        // When
        Mono<Map<String, Object>> result = keycloakAuthService.login("user", "pass");

        // Then
        StepVerifier.create(result)
                .expectNextMatches(tokens -> tokens.get("access_token").equals("token123"))
                .verifyComplete();
    }
}
