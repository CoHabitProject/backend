package fr.esgi.service.space;

import fr.esgi.domain.dto.space.ColocationReqDto;
import fr.esgi.domain.dto.space.ColocationResDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.persistence.entity.space.Colocation;
import fr.esgi.persistence.entity.user.User;
import fr.esgi.persistence.repository.space.ColocationRepository;
import fr.esgi.persistence.repository.user.UserRepository;
import fr.esgi.service.AbstractTest;
import fr.esgi.service.space.mapper.ColocationMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mapstruct.factory.Mappers;
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
@Import(ColocationServiceTest.TestConfig.class)
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
public class ColocationServiceTest extends AbstractTest {

    @TestConfiguration
    @EnableAutoConfiguration(
            exclude = {
                    ServletWebServerFactoryAutoConfiguration.class,
                    ReactiveWebServerFactoryAutoConfiguration.class
            }
    )
    static class TestConfig {
        @Bean
        public ColocationService colocationService(
                ColocationRepository colocationRepository,
                UserRepository userRepository) {
            return new ColocationService(colocationRepository, userRepository, Mappers.getMapper(ColocationMapper.class));
        }
    }

    @Autowired
    private ColocationRepository colocationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ColocationService colocationService;

    private User managerUser;
    private User roommateUser;
    private User otherUser;

    @BeforeEach
    public void initUsers() {
        // Setup manager user
        managerUser = new User();
        managerUser.setEmail("manager@example.com");
        managerUser.setFirstName("John");
        managerUser.setLastName("Manager");
        managerUser.setKeyCloakSub(TEST_USER_ID);
        managerUser.setBirthDate(LocalDate.of(1990, 1, 1));

        // Setup roommate user
        roommateUser = new User();
        roommateUser.setEmail("roommate@example.com");
        roommateUser.setFirstName("Jane");
        roommateUser.setLastName("Roommate");
        roommateUser.setKeyCloakSub("roommate-sub");
        roommateUser.setBirthDate(LocalDate.of(1992, 1, 1));

        // Setup other user
        otherUser = new User();
        otherUser.setEmail("other@example.com");
        otherUser.setFirstName("Bob");
        otherUser.setLastName("Other");
        otherUser.setKeyCloakSub("other-sub");
        otherUser.setBirthDate(LocalDate.of(1988, 1, 1));
    }

    @AfterEach
    public void cleanUp() {
        colocationRepository.deleteAll();
        userRepository.deleteAll();
        this.cleanupSecurityContext();
    }

    @Test
    public void testCreateColocation_Success() throws
                                               TechnicalException {
        // Given
        userRepository.save(managerUser);
        this.initSecurityContextPlaceHolder();

        ColocationReqDto dto = new ColocationReqDto();
        dto.setName("Coloc Centre Ville");
        dto.setAddress("123 Rue de la Paix");
        dto.setCity("Paris");
        dto.setPostalCode("75001");

        // When
        ColocationResDto result = colocationService.createColocation(dto);

        // Then
        assertNotNull(result);
        assertEquals("Coloc Centre Ville", result.getName());
        assertEquals("Paris", result.getCity());
        assertEquals("123 Rue de la Paix", result.getAddress());
        assertEquals("75001", result.getPostalCode());
        assertEquals(1, result.getNumberOfPeople()); // Manager is added as roommate
        assertNotNull(result.getDateEntree());
        assertNotNull(result.getId());
        assertNotNull(result.getInvitationCode());
        assertNotNull(result.getManager());
        assertEquals("John", result.getManager().getFirstName());
        assertEquals("Manager", result.getManager().getLastName());
        assertEquals("manager@example.com", result.getManager().getEmail());

        // Verify in database
        Optional<Colocation> savedColocation = colocationRepository.findById(result.getId());
        assertTrue(savedColocation.isPresent());
        assertEquals(managerUser,
                     savedColocation.get()
                                    .getManager());
        assertTrue(savedColocation.get()
                                  .isRoommate(managerUser));
        assertNotNull(savedColocation.get()
                                     .getInvitationCode());
        assertEquals(result.getInvitationCode(), savedColocation.get().getInvitationCode());
    }

    @Test
    public void testCreateColocation_UserNotFound() {
        // Given
        this.initSecurityContextPlaceHolder();
        // No user saved with TEST_USER_ID

        ColocationReqDto dto = new ColocationReqDto();
        dto.setName("Test Coloc");

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> colocationService.createColocation(dto));

        assertEquals(404, exception.getCode());
        assertEquals("Utilisateur n'est pas trouvé", exception.getMessage());
    }

    @Test
    public void testUpdateColocation_Success() throws
                                               TechnicalException {
        // Given
        userRepository.save(managerUser);
        this.initSecurityContextPlaceHolder();

        Colocation colocation = new Colocation("Original Name", "Original Address", managerUser);
        colocation.setCity("Original City");
        colocation.setPostalCode("00000");
        colocation.setInvitationCode("ORIGINAL1");
        colocation = colocationRepository.save(colocation);

        ColocationReqDto updateDto = new ColocationReqDto();
        updateDto.setName("Updated Name");
        updateDto.setAddress("Updated Address");
        updateDto.setCity("Updated City");
        updateDto.setPostalCode("11111");

        // When
        ColocationResDto result = colocationService.updateColocation(colocation.getId(), updateDto);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated City", result.getCity());
        assertEquals("Updated Address", result.getAddress());
        assertEquals("11111", result.getPostalCode());
        assertEquals("ORIGINAL1", result.getInvitationCode()); // Should remain unchanged
        assertNotNull(result.getManager());
        assertEquals("John", result.getManager().getFirstName());
        assertEquals("Manager", result.getManager().getLastName());
    }

    @Test
    public void testUpdateColocation_NotManager() {
        // Given
        List<User> users = List.of(managerUser, otherUser);
        userRepository.saveAll(users);

        Colocation colocation = new Colocation("Test Coloc", "Test Address", managerUser);
        colocation = colocationRepository.save(colocation);

        // Change authenticated user to otherUser
        otherUser.setKeyCloakSub("other-sub");
        userRepository.save(otherUser);
        this.initSecurityContextPlaceHolderWithSub("other-sub");

        ColocationReqDto updateDto = new ColocationReqDto();
        updateDto.setName("Updated Name");

        Long colocationId = colocation.getId();

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> colocationService.updateColocation(colocationId, updateDto));

        assertEquals(403, exception.getCode());
        assertEquals("Seul le gestionnaire peut modifier cette colocation", exception.getMessage());
    }

    @Test
    public void testGetColocationById_Success() throws
                                                TechnicalException {
        // Given
        userRepository.save(managerUser);
        Colocation colocation = new Colocation("Test Coloc", "Test Address", managerUser);
        colocation.setCity("Test City");
        colocation.setPostalCode("12345");
        colocation.setInvitationCode("TEST5678");
        colocation = colocationRepository.save(colocation);

        // When
        ColocationResDto result = colocationService.getColocationById(colocation.getId());

        // Then
        assertNotNull(result);
        assertEquals("Test Coloc", result.getName());
        assertEquals("Test City", result.getCity());
        assertEquals("Test Address", result.getAddress());
        assertEquals("12345", result.getPostalCode());
        assertEquals("TEST5678", result.getInvitationCode());
        assertNotNull(result.getManager());
        assertEquals("John", result.getManager().getFirstName());
        assertEquals("Manager", result.getManager().getLastName());
    }

    @Test
    public void testGetColocationById_NotFound() {
        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> colocationService.getColocationById(999L));

        assertEquals(404, exception.getCode());
        assertEquals("Colocation non trouvée", exception.getMessage());
    }

    @Test
    public void testGetManagedColocations_Success() throws
                                                    TechnicalException {
        // Given
        userRepository.save(managerUser);
        this.initSecurityContextPlaceHolder();

        Colocation coloc1 = new Colocation("Coloc 1", "Address 1", managerUser);
        Colocation coloc2 = new Colocation("Coloc 2", "Address 2", managerUser);
        colocationRepository.saveAll(List.of(coloc1, coloc2));

        // When
        List<ColocationResDto> result = colocationService.getManagedColocations();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream()
                         .anyMatch(c -> c.getName()
                                         .equals("Coloc 1")));
        assertTrue(result.stream()
                         .anyMatch(c -> c.getName()
                                         .equals("Coloc 2")));
    }

    @Test
    public void testGetUserColocations_Success() throws
                                                 TechnicalException {
        // Given
        List<User> users = List.of(managerUser, roommateUser);
        userRepository.saveAll(users);

        Colocation colocation = new Colocation("Test Coloc", "Test Address", managerUser);
        colocation.addRoommate(roommateUser);
        colocationRepository.save(colocation);

        // Set roommateUser as authenticated user
        roommateUser.setKeyCloakSub("roommate-sub");
        userRepository.save(roommateUser);
        this.initSecurityContextPlaceHolder();

        // When
        List<ColocationResDto> result = colocationService.getUserColocations();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Coloc",
                     result.get(0)
                           .getName());
        assertEquals(result.get(0).getManager().getEmail(), managerUser.getEmail());
        assertEquals(result.get(0).getUsers().size(), 2); // Manager + Roommate
    }

    @Test
    public void testDeleteColocation_Success() throws
                                               TechnicalException {
        // Given
        userRepository.save(managerUser);
        this.initSecurityContextPlaceHolder();

        Colocation colocation = new Colocation("Test Coloc", "Test Address", managerUser);
        colocation = colocationRepository.save(colocation);
        Long colocationId = colocation.getId();

        // When
        colocationService.deleteColocation(colocationId);

        // Then
        Optional<Colocation> deletedColocation = colocationRepository.findById(colocationId);
        assertFalse(deletedColocation.isPresent());
    }

    @Test
    public void testDeleteColocation_NotManager() {
        // Given
        List<User> users = List.of(managerUser, otherUser);
        userRepository.saveAll(users);

        Colocation colocation = new Colocation("Test Coloc", "Test Address", managerUser);
        colocation = colocationRepository.save(colocation);

        // Change authenticated user to otherUser
        otherUser.setKeyCloakSub("other-sub");
        userRepository.save(otherUser);
        this.initSecurityContextPlaceHolderWithSub("other-sub");

        Long colocationId = colocation.getId();

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> colocationService.deleteColocation(colocationId));

        assertEquals(403, exception.getCode());
        assertEquals("Seul le gestionnaire peut supprimer cette colocation", exception.getMessage());
    }

    @Test
    public void testJoinColocation_Success() throws
                                             TechnicalException {
        // Given
        List<User> users = List.of(managerUser, roommateUser);
        userRepository.saveAll(users);

        Colocation colocation = new Colocation("Test Coloc", "Test Address", managerUser);
        colocation.setInvitationCode("TEST1234");
        colocation = colocationRepository.save(colocation);

        // Set roommateUser as authenticated user
        roommateUser.setKeyCloakSub("rommate-sub");
        userRepository.save(roommateUser);
        this.initSecurityContextPlaceHolderWithSub("rommate-sub");

        // When
        ColocationResDto result = colocationService.joinColocation("TEST1234");

        // Then
        assertNotNull(result);
        assertEquals("Test Coloc", result.getName());
        assertEquals("TEST1234", result.getInvitationCode());
        assertTrue(result.getNumberOfPeople() > 0);
        assertNotNull(result.getManager());
        assertEquals("John", result.getManager().getFirstName());
        assertEquals("Manager", result.getManager().getLastName());

        // Verify in database
        Colocation updatedColocation = colocationRepository.findById(colocation.getId())
                                                           .orElse(null);
        assertNotNull(updatedColocation);
        assertTrue(updatedColocation.isRoommate(roommateUser));
    }

    @Test
    public void testJoinColocation_InvalidCode() {
        // Given
        userRepository.save(roommateUser);
        roommateUser.setKeyCloakSub(TEST_USER_ID);
        userRepository.save(roommateUser);
        this.initSecurityContextPlaceHolder();

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> colocationService.joinColocation("INVALID"));

        assertEquals(404, exception.getCode());
        assertEquals("Code d'invitation invalide", exception.getMessage());
    }

    @Test
    public void testJoinColocation_AlreadyMember() {
        // Given
        List<User> users = List.of(managerUser, roommateUser);
        userRepository.saveAll(users);

        Colocation colocation = new Colocation("Test Coloc", "Test Address", managerUser);
        colocation.setInvitationCode("TEST1234");
        colocation.addRoommate(roommateUser); // Already a member
        colocation = colocationRepository.save(colocation);

        roommateUser.setKeyCloakSub("roommate-sub");
        userRepository.save(roommateUser);
        this.initSecurityContextPlaceHolderWithSub("roommate-sub");

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> colocationService.joinColocation("TEST1234"));

        assertEquals(409, exception.getCode());
        assertEquals("Vous êtes déjà membre de cette colocation", exception.getMessage());
    }

    @Test
    public void testLeaveColocation_Success() throws
                                              TechnicalException {
        // Given
        List<User> users = List.of(managerUser, roommateUser);
        userRepository.saveAll(users);

        Colocation colocation = new Colocation("Test Coloc", "Test Address", managerUser);
        colocation.addRoommate(roommateUser);
        colocation = colocationRepository.save(colocation);

        roommateUser.setKeyCloakSub("roommate-sub");
        userRepository.save(roommateUser);
        this.initSecurityContextPlaceHolderWithSub("roommate-sub");

        // When
        colocationService.leaveColocation(colocation.getId());

        // Then
        Colocation updatedColocation = colocationRepository.findById(colocation.getId())
                                                           .orElse(null);
        assertNotNull(updatedColocation);
        assertFalse(updatedColocation.isRoommate(roommateUser));
    }

    @Test
    public void testLeaveColocation_ManagerCannotLeave() {
        // Given
        userRepository.save(managerUser);
        this.initSecurityContextPlaceHolder();

        Colocation colocation = new Colocation("Test Coloc", "Test Address", managerUser);
        colocation = colocationRepository.save(colocation);

        Long colocationId = colocation.getId();

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> colocationService.leaveColocation(colocationId));

        assertEquals(409, exception.getCode());
        assertEquals("Le gestionnaire ne peut pas quitter la colocation. Supprimez-la ou transférez la gestion.", exception.getMessage());
    }

    @Test
    public void testLeaveColocation_NotMember() {
        // Given
        List<User> users = List.of(managerUser, otherUser);
        userRepository.saveAll(users);

        Colocation colocation = new Colocation("Test Coloc", "Test Address", managerUser);
        colocation = colocationRepository.save(colocation);

        otherUser.setKeyCloakSub("other-sub");
        userRepository.save(otherUser);
        this.initSecurityContextPlaceHolderWithSub("other-sub");

        Long colocationId = colocation.getId();

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> colocationService.leaveColocation(colocationId));

        assertEquals(409, exception.getCode());
        assertEquals("Vous n'êtes pas membre de cette colocation", exception.getMessage());
    }

    @Test
    public void testUserNotAuthenticated() {
        // Given - no security context

        ColocationReqDto dto = new ColocationReqDto();
        dto.setName("Test");

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> colocationService.createColocation(dto));

        assertEquals(401, exception.getCode());
        assertEquals("User is not authenticated", exception.getMessage());
    }
}
