package fr.esgi.security.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Map;

class KeycloakAuthServiceTest {

    private static MockWebServer mockWebServer;
    private KeycloakAuthService keycloakAuthService;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(8888);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setup() {
        // Créer service avec les bonnes valeurs pour les tests
        String baseUrl = "http://localhost:" + mockWebServer.getPort();

        // Créer l'instance sans Spring
        keycloakAuthService = new KeycloakAuthService(
                baseUrl,
                "test-realm",
                "test-client",
                "test-secret"
        );

        // Optionnel: remplacer le WebClient par un qui pointe vers notre mockWebServer
        WebClient customWebClient = WebClient.builder()
                                             .baseUrl(baseUrl)
                                             .build();

        ReflectionTestUtils.setField(keycloakAuthService, "webClient", customWebClient);
    }

    @Test
    void login_ShouldReturnTokens_WhenSuccessful() {
        // Given
        String jsonResponse = "{\"access_token\":\"token123\",\"refresh_token\":\"refresh123\"}";

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(jsonResponse)
        );

        // When
        Mono<Map<String, Object>> result = keycloakAuthService.login("user", "pass");

        // Then
        StepVerifier.create(result)
                    .expectNextMatches(tokens ->
                                               "token123".equals(tokens.get("access_token")) &&
                                                       "refresh123".equals(tokens.get("refresh_token")))
                    .verifyComplete();
    }
}
