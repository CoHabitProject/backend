package fr.esgi.security.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class KeycloakAuthService {

    private final WebClient webClient;
    private final String tokenEndpoint;
    private final String clientId;
    private final String clientSecret;

    public KeycloakAuthService(
            @Value("${keycloak.auth-server-url}") String serverUrl,
            @Value("${keycloak.realm}") String realm,
            @Value("${keycloak.client.registration.id}") String clientId,
            @Value("${keycloak.client.registration.secret}") String clientSecret
    ) {
        this.tokenEndpoint = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.webClient = WebClient.builder().build();
    }

    public Mono<Map<String, Object>> login(String username, String password) {
        return webClient.post()
                        .uri(tokenEndpoint)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(BodyInserters
                                      .fromFormData("grant_type", "password")
                                      .with("client_id", clientId)
                                      .with("client_secret", clientSecret)
                                      .with("username", username)
                                      .with("password", password)
                        )
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError(),
                                  response -> response.bodyToMono(String.class)
                                                      .flatMap(error -> Mono.error(new RuntimeException("Keycloak error: " + error))))
                        .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    public Mono<Map<String,Object>> refresh(String refreshToken) {
        return webClient.post()
                        .uri(tokenEndpoint)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .bodyValue("grant_type=refresh_token"
                                           + "&client_id=" + clientId
                                           + "&client_secret=" + clientSecret
                                           + "&refresh_token=" + refreshToken)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<>() {});
    }
}

