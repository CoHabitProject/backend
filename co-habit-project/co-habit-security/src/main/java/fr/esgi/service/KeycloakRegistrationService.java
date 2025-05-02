package fr.esgi.service;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

    @Service
    public class KeycloakRegistrationService {

        private final Keycloak keycloak;
        private final String realm;

        public KeycloakRegistrationService(
                @Value("${keycloak.auth-server-url}") String serverUrl,
                @Value("${keycloak.realm}") String realm,
                @Value("${keycloak.client.registration.id}") String clientId,
                @Value("${keycloak.client.registration.secret}") String clientSecret
        ) {
            this.realm = realm;
            this.keycloak = KeycloakBuilder.builder()
                                           .serverUrl(serverUrl)
                                           .realm(realm)
                                           .clientId(clientId)
                                           .clientSecret(clientSecret)
                                           .grantType("client_credentials")
                                           .build();
        }

        public void register(String username, String email, String password) throws UserAlreadyExistsException {
            // Check if user already exists by username
            List<UserRepresentation> existingUsers = keycloak.realm(realm)
                .users()
                .searchByEmail(username, true);

            if (!existingUsers.isEmpty()) {
                throw new UserAlreadyExistsException("Email for username already exists");
            }

            // Check if user already exists by email
            existingUsers = existingUsers = keycloak.realm(realm)
                .users()
                .search(email);keycloak.realm(realm)
                .users()
                .search(email, null);

            if (!existingUsers.isEmpty()) {
                throw new UserAlreadyExistsException("Email already exists");
            }

            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setEmail(email);
            user.setEnabled(true);

            CredentialRepresentation cred = new CredentialRepresentation();
            cred.setType(CredentialRepresentation.PASSWORD);
            cred.setValue(password);
            cred.setTemporary(false);

            user.setCredentials(List.of(cred));

            try {
                Response response = keycloak.realm(realm).users().create(user);
                int status = response.getStatus();

                if (status != 201) {
                    throw new RuntimeException("Failed to register user. Status: " + status);
                }
            } catch (WebApplicationException e) {
                if (e.getResponse().getStatus() == 409) {
                    throw new UserAlreadyExistsException("User already exists");
                }
                throw new RuntimeException("Failed to register user", e);
            }
        }

        // Custom exception for user already exists scenario
        public static class UserAlreadyExistsException extends Exception {
            public UserAlreadyExistsException(String message) {
                super(message);
            }
        }
    }
