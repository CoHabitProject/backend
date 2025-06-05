package fr.esgi.security.service;

import fr.esgi.domain.dto.auth.RegisterReqDto;
import fr.esgi.domain.dto.user.UserProfileResDto;
import fr.esgi.domain.exception.FunctionalException;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.domain.port.in.IUserService;
import fr.esgi.domain.roles.UserRole;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.log4j.Log4j2;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class KeycloakRegistrationService implements IUserService {

    private final Keycloak keycloak;
    private final String   realm;

    @Autowired
    public KeycloakRegistrationService(
            Keycloak keycloak,
            @Value("${keycloak.realm}") String realm
    ) {
        this.realm    = realm;
        this.keycloak = keycloak;
    }

    @Override
    public String register(RegisterReqDto registerDto) throws
                                                       TechnicalException {
        String username = registerDto.getUsername();
        String email    = registerDto.getEmail();
        String password = registerDto.getPassword();

        // Check if user already exists by username
        List<UserRepresentation> existingUsers = keycloak.realm(realm)
                                                         .users()
                                                         .searchByEmail(email, true);

        if (!existingUsers.isEmpty()) {
            throw new TechnicalException(400, "Email for username already exists in identity provider");
        }

        // Check if user already exists by email
        existingUsers = keycloak.realm(realm)
                                .users()
                                .search(email);

        if (!existingUsers.isEmpty()) {
            throw new TechnicalException(400, "Email already exists");
        }

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setEnabled(true);
        user.setFirstName(registerDto.getFirstName());
        user.setLastName(registerDto.getLastName());

        // Store additional attributes
        Map<String, List<String>> attributes = Map.of(
                "birthDate", List.of(registerDto.getBirthDate()),
                "gender", List.of(registerDto.getGender()),
                "phoneNumber", List.of(registerDto.getPhoneNumber())
        );
        user.setAttributes(attributes);

        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(password);
        cred.setTemporary(false);

        user.setCredentials(List.of(cred));

        try {
            Response response = keycloak.realm(realm)
                                        .users()
                                        .create(user);
            int status = response.getStatus();

            if (status != 201) {
                throw new RuntimeException("Failed to register user. Status: " + status);
            }

            // Extract user ID from response and assign default role
            String userId = extractUserIdFromResponse(response);
            assignDefaultRole(userId, "USR1");
            return userId;

        } catch (WebApplicationException e) {
            if (e.getResponse()
                 .getStatus() == 409) {
                throw new TechnicalException(400, "User already exists");
            }
            throw new RuntimeException("Failed to register user", e);
        }
    }

    @Override
    public UserProfileResDto getUserProfile() throws
                                           TechnicalException {
        log.info("getUserProfile() called without Keycloak sub. This method is not implemented. \n Use getUserProfile(String keycloakSub) instead.");
        throw new TechnicalException(501, "Server error");
    }

    @Override
    public UserProfileResDto getUserProfile(String keycloakSub) throws
                                                             TechnicalException {
        try {
            UserRepresentation userRepresentation = keycloak.realm(realm)
                                                            .users()
                                                            .get(keycloakSub)
                                                            .toRepresentation();

            if (userRepresentation == null) {
                throw new TechnicalException(404, "User not found in Keycloak");
            }

            return UserProfileResDto.builder()
                                    .keyCloakSub(userRepresentation.getId())
                                    .username(userRepresentation.getUsername())
                                    .email(userRepresentation.getEmail())
                                    .firstName(userRepresentation.getFirstName())
                                    .lastName(userRepresentation.getLastName())
                                    .fullName(userRepresentation.getFirstName() + " " + userRepresentation.getLastName())
                                    .birthDate(getAttributeValue(userRepresentation, "birthDate"))
                                    .gender(getAttributeValue(userRepresentation, "gender"))
                                    .phoneNumber(getAttributeValue(userRepresentation, "phoneNumber"))
                                    .build();

        } catch (Exception e) {
            log.error("Failed to retrieve user profile for Keycloak sub: {}, {}", keycloakSub, e);
            throw new TechnicalException(500, "Failed to retrieve user profile from Keycloak");
        }
    }

    private String getAttributeValue(UserRepresentation user, String attributeName) {
        if (user.getAttributes() != null && user.getAttributes()
                                                .containsKey(attributeName)) {
            List<String> values = user.getAttributes()
                                      .get(attributeName);
            return values != null && !values.isEmpty() ? values.get(0) : null;
        }
        return null;
    }

    private String extractUserIdFromResponse(Response response) {
        String locationHeader = response.getHeaderString("Location");
        return locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
    }

    private void assignDefaultRole(String userId, String roleName) {
        addRoleToUser(this.keycloak, realm, userId, roleName);
    }

    protected static void addRoleToUser(Keycloak keycloak, String realm, String userId, String roleName) {
        try {
            // Get the role representation
            RoleRepresentation role = keycloak.realm(realm)
                                              .roles()
                                              .get(roleName)
                                              .toRepresentation();

            // Assign the role to the user
            keycloak.realm(realm)
                    .users()
                    .get(userId)
                    .roles()
                    .realmLevel()
                    .add(List.of(role));
        } catch (Exception e) {
            throw new RuntimeException("Failed to add role " + roleName + " to user " + userId, e);
        }
    }

    public static void promoteUserToManager(Keycloak keycloak, String realm, String userId) {
        try {
            // Get the role representation
            RoleRepresentation role = keycloak.realm(realm)
                                              .roles()
                                              .get(UserRole.USR2.name())
                                              .toRepresentation();

            // Assign the role to the user
            keycloak.realm(realm)
                    .users()
                    .get(userId)
                    .roles()
                    .realmLevel()
                    .add(List.of(role));
        } catch (Exception e) {
            throw new FunctionalException("Failed to add role " + UserRole.USR2.getDescription() + " to user " + userId, e);
        }
    }

    public static void demoteManagerToUser(Keycloak keycloak, String realm, String userId) {
        try {
            // Get the role representation
            RoleRepresentation role = keycloak.realm(realm)
                                              .roles()
                                              .get(UserRole.USR2.name())
                                              .toRepresentation();

            // Remove the role from the user
            keycloak.realm(realm)
                    .users()
                    .get(userId)
                    .roles()
                    .realmLevel()
                    .remove(List.of(role));
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove role " + UserRole.USR2.getDescription() + " from user " + userId, e);
        }
    }

}
