package fr.esgi.service.space.mapper;

import fr.esgi.domain.DateUtils;
import fr.esgi.domain.dto.space.ColocationReqDto;
import fr.esgi.domain.dto.space.ColocationResDto;
import fr.esgi.domain.dto.user.UserProfileResDto;
import fr.esgi.persistence.entity.space.Colocation;
import fr.esgi.persistence.entity.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColocationMapperTest {

    private ColocationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ColocationMapper.class);
    }

    @Test
    void shouldMapDtoToColocation() {
        ColocationReqDto dto = new ColocationReqDto();
        dto.setName("Test Coloc");
        dto.setAddress("123 Rue Exemple");
        Colocation entity = mapper.mapDtoToColocation(dto);

        assertNotNull(entity);
        assertNull(entity.getId());
        assertEquals("Test Coloc", entity.getName());
        assertEquals("123 Rue Exemple", entity.getAddress());
        assertNull(entity.getDescription());
        assertNull(entity.getInvitationCode());
        assertNull(entity.getMaxRoommates());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
        assertNull(entity.getManager());
        assertTrue(entity.getRoommates().isEmpty());
    }

    @Test
    void shouldMapColocationToResDto() {
        // Prepare
        Colocation coloc = new Colocation();
        coloc.setId(42L);
        coloc.setName("Ma Coloc");
        coloc.setAddress("45 Av. Test");
        coloc.setInvitationCode("ABC123");
        LocalDateTime created = LocalDateTime.of(2025, 6, 1, 8, 30);
        LocalDateTime updated = LocalDateTime.of(2025, 6, 15, 12, 0);
        coloc.setCreatedAt(created);
        coloc.setUpdatedAt(updated);

        // manager
        User manager = new User();
        manager.setId(7L);
        manager.setFirstName("Alice");
        manager.setLastName("Dupont");
        manager.setEmail("alice@example.com");
        coloc.setManager(manager);

        // roommates
        User u1 = new User();
        u1.setId(5L);
        u1.setFirstName("Bob");
        u1.setLastName("Martin");
        u1.setEmail("bob@example.com");
        u1.setBirthDate(LocalDate.of(1990, 1, 2));

        User u2 = new User();
        u2.setId(6L);
        u2.setFirstName("Carla");
        u2.setLastName("Durand");
        u2.setEmail("carla@example.com");
        // pas de birthDate => null

        coloc.setRoommates(Set.of(u1, u2));

        // When
        ColocationResDto res = mapper.mapColocationToResDto(coloc);

        // Then
        assertNotNull(res);
        assertEquals(42L, res.getId());
        assertEquals("Ma Coloc", res.getName());
        assertEquals("45 Av. Test", res.getAddress());
        assertEquals("ABC123", res.getInvitationCode());
        assertEquals(2, res.getNumberOfPeople());
        assertEquals(DateUtils.localDateToString(created.toLocalDate()), res.getDateEntree());

        // createdAt && updatedAt in ISO format
        assertEquals(created.toString(), res.getCreatedAt());
        assertEquals(updated.toString(), res.getUpdatedAt());

        assertNotNull(res.getManager());
        assertEquals("Alice", res.getManager().getFirstName());
        assertEquals("Dupont", res.getManager().getLastName());
        assertEquals("alice@example.com", res.getManager().getEmail());

        List<UserProfileResDto> users = res.getUsers();
        assertEquals(2, users.size());

        // find user in colocation
        assertTrue(users.stream().anyMatch(u -> "Bob".equals(u.getFirstName()) && "Martin".equals(u.getLastName())
                && "bob@example.com".equals(u.getEmail()) && "1990-01-02".equals(u.getBirthDate())));
        assertTrue(users.stream().anyMatch(u -> "Carla".equals(u.getFirstName()) && "Durand".equals(u.getLastName())
                && "carla@example.com".equals(u.getEmail()) && u.getBirthDate() == null));
    }

    @Test
    void shouldMapListOfColocations() {
        Colocation c1 = new Colocation(); c1.setId(1L); c1.setRoommates(Set.of());
        Colocation c2 = new Colocation(); c2.setId(2L); c2.setRoommates(Set.of());
        List<ColocationResDto> dtos = mapper.mapColocationsToResDtos(List.of(c1, c2));
        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        assertEquals(1L, dtos.get(0).getId());
        assertEquals(2L, dtos.get(1).getId());
    }

    @Test
    void shouldUpdateExistingColocation() {
        ColocationReqDto dto = new ColocationReqDto();
        dto.setName("Nouvelle");
        dto.setAddress("Nouvelle adresse");

        Colocation existing = new Colocation();
        existing.setId(99L);
        existing.setName("Ancienne");
        existing.setAddress("Ancienne adresse");
        existing.setDescription("Garde");
        existing.setInvitationCode("XYZ");
        existing.setMaxRoommates(5);
        existing.setCreatedAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setManager(new User());
        existing.setRoommates(Set.of(new User()));

        mapper.updateColocationFromDto(dto, existing);

        assertEquals(99L, existing.getId());
        assertEquals("Nouvelle", existing.getName());
        assertEquals("Nouvelle adresse", existing.getAddress());
        assertEquals("Garde", existing.getDescription());
        assertEquals("XYZ", existing.getInvitationCode());
        assertEquals(5, existing.getMaxRoommates());
        assertNotNull(existing.getCreatedAt());
        assertNotNull(existing.getUpdatedAt());
        assertNotNull(existing.getManager());
        assertFalse(existing.getRoommates().isEmpty());
    }

    @Test
    void shouldHandleNullsGracefully() {
        ColocationResDto res = mapper.mapColocationToResDto(new Colocation());
        assertNotNull(res);
        assertNull(res.getInvitationCode());
        assertEquals(0, res.getNumberOfPeople());
        assertNull(res.getDateEntree());
        assertNull(res.getCreatedAt());
        assertNull(res.getUpdatedAt());
        assertNotNull(res.getUsers());
        assertTrue(res.getUsers().isEmpty());
        ColocationReqDto dto = new ColocationReqDto();
        Colocation e = mapper.mapDtoToColocation(dto);
        assertNotNull(e);
        assertTrue(e.getRoommates().isEmpty());
    }
}
