package fr.esgi.service.space;

import fr.esgi.domain.dto.user.UserRelationshipReqDto;
import fr.esgi.domain.dto.user.UserRelationshipResDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.domain.port.in.IUserRelationService;
import fr.esgi.persistence.entity.user.User;
import fr.esgi.persistence.entity.user.UserRelationship;
import fr.esgi.persistence.repository.user.UserRelationshipRepository;
import fr.esgi.persistence.repository.user.UserRepository;
import fr.esgi.service.AbstractTest;
import fr.esgi.service.space.mapper.UserRelationshipMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(UserRelationServiceTest.TestConfig.class)
@TestPropertySource(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
        }
)
@EnableJpaRepositories(basePackages = "fr.esgi.persistence.repository")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserRelationServiceTest extends AbstractTest {

    @TestConfiguration
    @EnableAutoConfiguration(
            exclude = {
                    ServletWebServerFactoryAutoConfiguration.class,
                    ReactiveWebServerFactoryAutoConfiguration.class
            }
    )
    static class TestConfig {
        @Bean
        public UserRelationService userRelationService(
                UserRepository userRepository,
                UserRelationshipRepository userRelationshipRepository) {
            return new UserRelationService(userRepository, userRelationshipRepository, UserRelationshipMapper.INSTANCE);
        }
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRelationshipRepository userRelationshipRepository;

    @Autowired
    private UserRelationService userRelationService;

    private User parentUser;
    private User childUser;
    private User childBisUser;

    @BeforeEach
    public void initUsers() {
        // Setup parent user
        parentUser = new User();
        parentUser.setEmail("john-parent@exemple.com");
        parentUser.setFirstName("John-Parent");
        parentUser.setLastName("Doe-Parent");
        parentUser.setKeyCloakSub("parent-sub");
        parentUser.setBirthDate(LocalDate.of(1980, 1, 1));


        // Setup child user
        childUser = new User();
        childUser.setEmail("john-child@gmail.com");
        childUser.setFirstName("John-Child");
        childUser.setLastName("Doe-Child");
        childUser.setBirthDate(LocalDate.of(2000, 1, 1));

        // Setup child bis user
        childBisUser = new User();
        childBisUser.setEmail("john-child-bis@gmail.com");
        childBisUser.setFirstName("John-Child-Bis");
        childBisUser.setLastName("Doe-Child-Bis");
        childBisUser.setBirthDate(LocalDate.of(2001, 1, 1));
    }

    @AfterEach
    public void cleanUp() {
        // Clear repositories
        userRelationshipRepository.deleteAll();
        userRepository.deleteAll();

        // Clear security context
        this.cleanupSecurityContext();
    }

    @Test
    public void testSetUserRelationsFromParent() throws
                                                 TechnicalException {
        // Given
        parentUser.setKeyCloakSub(TEST_USER_ID);
        List<User> userList = List.of(parentUser, childBisUser, childUser);
        userRepository.saveAll(userList);

        this.initSecurityContextPlaceHolder();

        UserRelationshipReqDto dto = UserRelationshipReqDto.builder()
                                                           .firstName("John-Child")
                                                           .lastName("Doe-Child")
                                                           .birthDate("2000-01-01")
                                                           .whoAmI(IUserRelationService.PARENT)
                                                           .build();

        // When
        UserRelationshipResDto res = userRelationService.requestRelation(dto);

        // Then
        assertNotNull(res);
        assertEquals("john-child@gmail.com", res.getChildEmail());
        assertEquals("john-parent@exemple.com", res.getParentEmail());
        assertTrue(res.isParentConfirmed());
        assertFalse(res.isChildConfirmed());
        assertFalse(res.isFullyConfirmed());

        // Verify relationship is saved in database
        Optional<UserRelationship> savedRelationship = userRelationshipRepository.findByParentAndChild(parentUser, childUser);
        assertTrue(savedRelationship.isPresent());
        assertTrue(savedRelationship.get()
                                    .isParentConfirmed());
        assertFalse(savedRelationship.get()
                                     .isChildConfirmed());
    }

    @Test
    public void testSetUserRelationsFromChild() throws
                                                TechnicalException {
        // Given
        childUser.setKeyCloakSub(TEST_USER_ID);
        List<User> userList = List.of(parentUser, childBisUser, childUser);
        userRepository.saveAll(userList);

        this.initSecurityContextPlaceHolder();

        UserRelationshipReqDto dto = UserRelationshipReqDto.builder()
                                                           .firstName("John-Parent")
                                                           .lastName("Doe-Parent")
                                                           .birthDate("1980-01-01")
                                                           .whoAmI(IUserRelationService.CHILD)
                                                           .build();

        // When
        UserRelationshipResDto res = userRelationService.requestRelation(dto);

        // Then
        assertNotNull(res);
        assertEquals("john-child@gmail.com", res.getChildEmail());
        assertEquals("john-parent@exemple.com", res.getParentEmail());
        assertFalse(res.isParentConfirmed());
        assertTrue(res.isChildConfirmed());
        assertFalse(res.isFullyConfirmed());

        // Verify relationship is saved in database
        Optional<UserRelationship> savedRelationship = userRelationshipRepository.findByParentAndChild(parentUser, childUser);
        assertTrue(savedRelationship.isPresent());
        assertFalse(savedRelationship.get()
                                     .isParentConfirmed());
        assertTrue(savedRelationship.get()
                                    .isChildConfirmed());
    }

    @Test
    public void testRequestRelation_ParentNotFound() {
        // Given
        this.initSecurityContextPlaceHolder();
        // Use non-existent keycloak sub

        UserRelationshipReqDto dto = UserRelationshipReqDto.builder()
                                                           .firstName("John-Child")
                                                           .lastName("Doe-Child")
                                                           .birthDate("2000-01-01")
                                                           .whoAmI(IUserRelationService.PARENT)
                                                           .build();

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> userRelationService.requestRelation(dto));

        assertEquals(404, exception.getCode());
        assertEquals("Utilisateur n'est pas trouvé", exception.getMessage());
    }

    @Test
    public void testRequestRelation_ChildNotFound() {
        // Given
        childUser.setKeyCloakSub(TEST_USER_ID);
        List<User> userList = List.of(parentUser, childBisUser, childUser);
        userRepository.saveAll(userList);

        this.initSecurityContextPlaceHolder();

        UserRelationshipReqDto dto = UserRelationshipReqDto.builder()
                                                           .firstName("NonExistent")
                                                           .lastName("Child")
                                                           .birthDate("2000-01-01")
                                                           .whoAmI(IUserRelationService.PARENT)
                                                           .build();

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> userRelationService.requestRelation(dto));

        assertEquals(404, exception.getCode());
        assertEquals("Votre enfant n'est pas trouvé", exception.getMessage());
    }

    @Test
    public void testGetRelationShip_WhenUserAuthenticate() {
        // given
        parentUser.setKeyCloakSub(TEST_USER_ID);
        List<User> userList = List.of(parentUser, childBisUser, childUser);
        userRepository.saveAll(userList);
        this.initSecurityContextPlaceHolder();

        // Create a relationship first
        UserRelationship relationship = UserRelationship.builder()
                                                        .parent(parentUser)
                                                        .child(childUser)
                                                        .parentConfirmed(true)
                                                        .childConfirmed(true)
                                                        .build();
        userRelationshipRepository.save(relationship);

        // when & then - add your specific test logic here
        Optional<User> foundUser = userRepository.findByKeyCloakSub(TEST_USER_ID);
        assertTrue(foundUser.isPresent());
    }

    @Test
    public void testGetAllRelationsForUser_WhenUserHasMultipleRelationships() throws
                                                                              TechnicalException {
        // Given
        parentUser.setKeyCloakSub(TEST_USER_ID);
        List<User> userList = List.of(parentUser, childBisUser, childUser);
        userRepository.saveAll(userList);
        this.initSecurityContextPlaceHolder();

        // Create multiple relationships for the parent user
        UserRelationship relationship1 = UserRelationship.builder()
                                                         .parent(parentUser)
                                                         .child(childUser)
                                                         .parentConfirmed(true)
                                                         .childConfirmed(true)
                                                         .build();

        UserRelationship relationship2 = UserRelationship.builder()
                                                         .parent(parentUser)
                                                         .child(childBisUser)
                                                         .parentConfirmed(true)
                                                         .childConfirmed(false)
                                                         .build();

        userRelationshipRepository.saveAll(List.of(relationship1, relationship2));

        // When
        List<UserRelationshipResDto> relationships = userRelationService.getAllRelationsForUser();

        // Then
        assertNotNull(relationships);
        assertEquals(2, relationships.size());

        // Verify first relationship
        UserRelationshipResDto rel1 = relationships.stream()
                                                   .filter(r -> r.getChildEmail()
                                                                 .equals("john-child@gmail.com"))
                                                   .findFirst()
                                                   .orElse(null);
        assertNotNull(rel1);
        assertTrue(rel1.isParentConfirmed());
        assertTrue(rel1.isChildConfirmed());
        assertTrue(rel1.isFullyConfirmed());

        // Verify second relationship
        UserRelationshipResDto rel2 = relationships.stream()
                                                   .filter(r -> r.getChildEmail()
                                                                 .equals("john-child-bis@gmail.com"))
                                                   .findFirst()
                                                   .orElse(null);
        assertNotNull(rel2);
        assertTrue(rel2.isParentConfirmed());
        assertFalse(rel2.isChildConfirmed());
        assertFalse(rel2.isFullyConfirmed());
    }

    @Test
    public void testGetAllRelationsForUser_WhenUserIsChild() throws
                                                             TechnicalException {
        // Given
        childUser.setKeyCloakSub(TEST_USER_ID);
        List<User> userList = List.of(parentUser, childBisUser, childUser);
        userRepository.saveAll(userList);
        this.initSecurityContextPlaceHolder();

        // Create relationship where the authenticated user is a child
        UserRelationship relationship = UserRelationship.builder()
                                                        .parent(parentUser)
                                                        .child(childUser)
                                                        .parentConfirmed(false)
                                                        .childConfirmed(true)
                                                        .build();

        userRelationshipRepository.save(relationship);

        // When
        List<UserRelationshipResDto> relationships = userRelationService.getAllRelationsForUser();

        // Then
        assertNotNull(relationships);
        assertEquals(1, relationships.size());

        UserRelationshipResDto rel = relationships.get(0);
        assertEquals("john-child@gmail.com", rel.getChildEmail());
        assertEquals("john-parent@exemple.com", rel.getParentEmail());
        assertFalse(rel.isParentConfirmed());
        assertTrue(rel.isChildConfirmed());
        assertFalse(rel.isFullyConfirmed());
    }

    @Test
    public void testGetAllRelationsForUser_WhenUserHasNoRelationships() throws
                                                                        TechnicalException {
        // Given
        parentUser.setKeyCloakSub(TEST_USER_ID);
        userRepository.save(parentUser);
        this.initSecurityContextPlaceHolder();

        // When
        List<UserRelationshipResDto> relationships = userRelationService.getAllRelationsForUser();

        // Then
        assertNotNull(relationships);
        assertTrue(relationships.isEmpty());
    }

    @Test
    public void testGetAllRelationsForUser_WhenUserNotFound() {
        // Given
        this.initSecurityContextPlaceHolder();
        // No user saved with TEST_USER_ID

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> userRelationService.getAllRelationsForUser());

        assertEquals(404, exception.getCode());
        assertEquals("Utilisateur n'est pas trouvé", exception.getMessage());
    }

    @Test
    public void testGetAllRelationsForUser_WhenUserNotAuthenticated() {
        // Given - no security context

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> userRelationService.getAllRelationsForUser());

        assertEquals(401, exception.getCode());
        assertEquals("User is not authenticated", exception.getMessage());
    }
}
