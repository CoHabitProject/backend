package fr.esgi.rest.interne;

import fr.esgi.domain.dto.user.UserProfileResDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.persistence.entity.user.User;
import fr.esgi.persistence.repository.user.UserRepository;
import fr.esgi.security.service.KeycloakRegistrationService;
import fr.esgi.service.mapper.UserMapper;
import fr.esgi.service.registration.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileRestTest {

    @Mock
    private KeycloakRegistrationService keycloakRegistrationService;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserMapper userMapper;

    private ProfileRest profileRest;
    private UserService userService;
    
    private static final String TEST_USER_ID = "122";
    private static final String TEST_USERNAME = "johndoe";
    private static final String TEST_EMAIL = "john@example.com";
    private static final LocalDateTime TEST_CREATED_AT = LocalDateTime.now();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_THREADLOCAL);
        SecurityContextHolder.clearContext();
        
        // Real UserService with mocked dependencies
        userService = new UserService(keycloakRegistrationService, userRepository, userMapper);
        profileRest = new ProfileRest(userService);
    }

    @Test
    void getUserProfile_ShouldReturn200_WhenUserIsAuthenticated() throws TechnicalException {
        // Arrange
        setupAuthenticationContext();
        
        User mockUser = new User();
        mockUser.setKeyCloakSub(TEST_USER_ID);
        mockUser.setUsername(TEST_USERNAME);
        mockUser.setEmail(TEST_EMAIL);
        
        when(userRepository.findByKeyCloakSub(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        
        UserProfileResDto expectedProfile = UserProfileResDto.builder()
                                                             .id(1L)
                                                             .keyCloakSub(TEST_USER_ID)
                                                             .username(TEST_USERNAME)
                                                             .email(TEST_EMAIL)
                                                             .firstName("John")
                                                             .lastName("Doe")
                                                             .fullName("John Doe")
                                                             .createdAt(TEST_CREATED_AT)
                                                             .build();
                
        when(userMapper.mapUserToProfileDto(mockUser)).thenReturn(expectedProfile);

        // Act
        ResponseEntity<?> response = profileRest.getUserProfile();

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isInstanceOf(UserProfileResDto.class);

        UserProfileResDto user = (UserProfileResDto) response.getBody();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getKeyCloakSub()).isEqualTo(TEST_USER_ID);
        assertThat(user.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(user.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getFullName()).isEqualTo("John Doe");
        assertThat(user.getCreatedAt()).isEqualTo(TEST_CREATED_AT);
    }

    @Test
    void getUserProfile_ShouldReturn401_WhenUserIsNotAuthenticated() {
        // Arrange
        // Clear in the beforeEach method, so no authentication is set up

        // Act & Assert
        try {
            ResponseEntity<?> response = profileRest.getUserProfile();
            assertThat(response.getStatusCodeValue()).isEqualTo(401);
        } catch (TechnicalException e) {
            assertThat(e.getMessage()).isEqualTo("User is not authenticated");
        }
    }

    @Test
    void getUserProfile_ShouldReturn404_WhenUserNotFoundInRepository() {
        // Arrange
        setupAuthenticationContext();
        
        when(userRepository.findByKeyCloakSub(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        try {
            ResponseEntity<?> response = profileRest.getUserProfile();
            assertThat(response.getStatusCodeValue()).isEqualTo(404);
        } catch (TechnicalException e) {
            assertThat(e.getMessage()).isEqualTo("User not found");
        }
    }
    
    private void setupAuthenticationContext() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        
        Jwt jwt = Jwt.withTokenValue("token")
                .claim("sub", TEST_USER_ID)
                .claim("preferred_username", TEST_USERNAME)
                .claim("email", TEST_EMAIL)
                .claim("email_verified", true)
                .claim("realm_access", Map.of("roles", List.of("USER", "ADMIN")))
                .header("alg", "none")
                .build();

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, authorities);
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
