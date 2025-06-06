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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserRelationServiceTest extends AbstractTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRelationshipRepository userRelationshipRepository;

    @Mock
    private UserRelationshipMapper userRelationshipMapper;

    @InjectMocks
    UserRelationService userRelationService;

    private User parentUser;
    private User childUser;

    @BeforeAll
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeEach
    public void initUsers() {
        // Reset mocks before each test
        reset(userRepository, userRelationshipRepository, userRelationshipMapper);
        
        // Setup parent user
        parentUser = new User();
        parentUser.setId(1L);
        parentUser.setEmail("john@exemple.com");
        parentUser.setFirstName("John");
        parentUser.setLastName("Doe");
        parentUser.setKeyCloakSub("parent-sub");

        // Setup child user
        childUser = new User();
        childUser.setId(2L);
        childUser.setEmail("john-child@gmail.com");
        childUser.setFirstName("John-Child");
        childUser.setLastName("Doe-Child");
        childUser.setBirthDate(LocalDate.of(2000, 1, 1));
    }

    @Test
    public void testGetUserRelations() throws TechnicalException {
        // Given
        this.initSecurityContextPlaceHolder();

        UserRelationshipReqDto dto = UserRelationshipReqDto.builder()
                .firstName("John-Child")
                .lastName("Doe-Child")
                .birthDate("2000-01-01")
                .whoAmI(IUserRelationService.PARENT)
                .build();

        // Mock repository responses
        when(userRepository.findByKeyCloakSub(anyString()))
                .thenReturn(Optional.of(parentUser));
        
        when(userRepository.findByFirstNameAndLastNameAndBirthDate(
                "John-Child", 
                "Doe-Child", 
                LocalDate.of(2000, 1, 1)))
                .thenReturn(Optional.of(childUser));

        UserRelationship savedRelationship = UserRelationship.builder()
                .id(1L)
                .parent(parentUser)
                .child(childUser)
                .parentConfirmed(true)
                .childConfirmed(false)
                .build();

        when(userRelationshipRepository.save(any(UserRelationship.class)))
                .thenReturn(savedRelationship);

        UserRelationshipResDto expectedDto = UserRelationshipResDto.builder()
                .id(1L)
                .parentId(1L)
                .parentEmail("john@exemple.com")
                .childId(2L)
                .childEmail("john-child@gmail.com")
                .parentConfirmed(true)
                .childConfirmed(false)
                .fullyConfirmed(false)
                .build();

        when(userRelationshipMapper.toDto(any(UserRelationship.class)))
                .thenReturn(expectedDto);

        // When
        UserRelationshipResDto res = userRelationService.requestRelation(dto);

        // Then
        assertNotNull(res);
        assertEquals("john-child@gmail.com", res.getChildEmail());
        assertEquals("john@exemple.com", res.getParentEmail());
        assertTrue(res.isParentConfirmed());
        assertFalse(res.isChildConfirmed());
        assertFalse(res.isFullyConfirmed());

        // Verify interactions - use times(1) to be explicit
        verify(userRepository, times(1)).findByKeyCloakSub(anyString());
        verify(userRepository, times(1)).findByFirstNameAndLastNameAndBirthDate(
                "John-Child", "Doe-Child", LocalDate.of(2000, 1, 1));
        
        ArgumentCaptor<UserRelationship> relationshipCaptor = ArgumentCaptor.forClass(UserRelationship.class);
        verify(userRelationshipRepository, times(1)).save(relationshipCaptor.capture());
        
        UserRelationship capturedRelationship = relationshipCaptor.getValue();
        assertEquals(parentUser, capturedRelationship.getParent());
        assertEquals(childUser, capturedRelationship.getChild());
        assertTrue(capturedRelationship.isParentConfirmed());
        assertFalse(capturedRelationship.isChildConfirmed());
        
        verify(userRelationshipMapper, times(1)).toDto(any(UserRelationship.class));
    }

    @Test
    public void testRequestRelation_ParentNotFound() {
        // Given
        this.initSecurityContextPlaceHolder();

        UserRelationshipReqDto dto = UserRelationshipReqDto.builder()
                .firstName("John-Child")
                .lastName("Doe-Child")
                .birthDate("2000-01-01")
                .whoAmI(IUserRelationService.PARENT)
                .build();

        when(userRepository.findByKeyCloakSub(anyString()))
                .thenReturn(Optional.empty());

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class, 
                () -> userRelationService.requestRelation(dto));
        
        assertEquals(404, exception.getCode());
        assertEquals("Utilisateur n'est pas trouvé", exception.getMessage());
        
        // Verify only one call to findByKeyCloakSub
        verify(userRepository, times(1)).findByKeyCloakSub(anyString());
        // Verify that findByFirstNameAndLastNameAndBirthDate is never called
        verify(userRepository, never()).findByFirstNameAndLastNameAndBirthDate(anyString(), anyString(), any(LocalDate.class));
    }

    @Test
    public void testRequestRelation_ChildNotFound() {
        // Given
        this.initSecurityContextPlaceHolder();

        UserRelationshipReqDto dto = UserRelationshipReqDto.builder()
                .firstName("John-Child")
                .lastName("Doe-Child")
                .birthDate("2000-01-01")
                .whoAmI(IUserRelationService.PARENT)
                .build();

        when(userRepository.findByKeyCloakSub(anyString()))
                .thenReturn(Optional.of(parentUser));
        
        when(userRepository.findByFirstNameAndLastNameAndBirthDate(
                anyString(), anyString(), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class, 
                () -> userRelationService.requestRelation(dto));
        
        assertEquals(404, exception.getCode());
        assertEquals("Votre enfin n'est pas trouvé", exception.getMessage());
        
        // Verify interactions
        verify(userRepository, times(1)).findByKeyCloakSub(anyString());
        verify(userRepository, times(1)).findByFirstNameAndLastNameAndBirthDate(anyString(), anyString(), any(LocalDate.class));
        // Verify that save is never called
        verify(userRelationshipRepository, never()).save(any(UserRelationship.class));
    }
}
