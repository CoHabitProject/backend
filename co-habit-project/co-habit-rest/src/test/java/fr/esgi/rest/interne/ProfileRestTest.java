package fr.esgi.rest.interne;

import fr.esgi.domain.dto.user.UserProfileDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileRestTest {

    private final ProfileRest profileRest = new ProfileRest();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        // Forcer le ThreadLocal
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_THREADLOCAL);
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUserProfile_ShouldReturn200_WhenUserIsAuthenticated() {
        // Arrange
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Jwt jwt = Jwt.withTokenValue("token")
                .claim("sub", "123")
                .claim("preferred_username", "johndoe")
                .claim("email", "john@example.com")
                .claim("email_verified", true)
                .claim("realm_access", Map.of("roles", List.of("USER", "ADMIN")))
                .header("alg", "none")
                .build();

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, authorities);
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        ResponseEntity<?> response = profileRest.getUserProfile();

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isInstanceOf(UserProfileDto.class);

        UserProfileDto user = (UserProfileDto) response.getBody();
        assertThat(user.getId()).isEqualTo("123");
        assertThat(user.getUsername()).isEqualTo("johndoe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getRoles()).containsExactlyInAnyOrder("USER", "ADMIN");
    }

    @Test
    void getUserProfile_ShouldReturn401_WhenUserIsNotAuthenticated() {
        // Arrange — contexte vide

        // Act
        ResponseEntity<?> response = profileRest.getUserProfile();

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(401);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body)
                .containsEntry("authenticated", false)
                .containsEntry("message", "User is not authenticated");
    }
}
