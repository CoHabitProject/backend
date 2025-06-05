package fr.esgi.service.space;

import fr.esgi.domain.dto.user.UserRelationshipReqDto;
import fr.esgi.domain.dto.user.UserRelationshipResDto;
import fr.esgi.domain.port.in.IUserRelationService;
import fr.esgi.persistence.repository.user.UserRelationshipRepository;
import fr.esgi.service.AbstractTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserRelationServiceTest extends AbstractTest {

    @Mock
    private UserRelationshipRepository userRelationshipRepository;

    UserRelationService userRelationService;

    @BeforeAll
    public void setUp() {
        userRelationService = new UserRelationService();
    }

    @Test
    public void testGetUserRelations() {
        //        Given
        this.initSecurityContextPlaceHolder();

        UserRelationshipReqDto dto = UserRelationshipReqDto.builder()
                                                           .firstName("John-Child")
                                                           .lastName("Doe-Child")
                                                           .birthDate("2000-01-01")
                                                           .whoAmI(IUserRelationService.PARENT)
                                                           .build();

        //        When
        UserRelationshipResDto res = userRelationService.requestRelation(dto);
        //       Then


        //      Assertions
        assertEquals("john-child@gmail.com", res.getChildEmail());
        assertEquals("john@exemple.com", res.getParentEmail());
        assertTrue(res.isParentConfirmed());
        assertFalse(res.isChildConfirmed());
        assertFalse(res.isFullyConfirmed());

    }


}
