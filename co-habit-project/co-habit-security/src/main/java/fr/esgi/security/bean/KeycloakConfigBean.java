package fr.esgi.security.bean;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@Configuration
public class KeycloakConfigBean {

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client.registration.id}")
    private String clientId;

    @Value("${keycloak.client.registration.secret}")
    private String clientSecret;

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                              .serverUrl(serverUrl)
                              .realm(realm)
                              .clientId(clientId)
                              .clientSecret(clientSecret)
                              .grantType(AuthorizationGrantType.CLIENT_CREDENTIALS.getValue())
                              .build();
    }

    @Bean
    public String keycloakRealm() {
        return realm;
    }
}
