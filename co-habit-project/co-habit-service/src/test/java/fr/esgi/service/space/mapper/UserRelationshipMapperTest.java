package fr.esgi.service.space.mapper;

import fr.esgi.domain.dto.user.UserRelationshipResDto;
import fr.esgi.persistence.entity.user.User;
import fr.esgi.persistence.entity.user.UserRelationship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class UserRelationshipMapperTest {

    private UserRelationshipMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(UserRelationshipMapper.class);
    }

    @Test
    void shouldMapEntityToDto() {
        // Given
        User parent = new User();
        parent.setId(10L);
        parent.setEmail("parent@example.com");

        User child = new User();
        child.setId(20L);
        child.setEmail("child@example.com");

        UserRelationship entity = UserRelationship.builder()
                                                  .id(99L)
                                                  .parent(parent)
                                                  .child(child)
                                                  .build();
        entity.setParentConfirmed(true);
        entity.setChildConfirmed(false);

        // When
        UserRelationshipResDto dto = mapper.toDto(entity);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(99L);
        assertThat(dto.getParentId()).isEqualTo(10L);
        assertThat(dto.getParentEmail()).isEqualTo("parent@example.com");
        assertThat(dto.getChildId()).isEqualTo(20L);
        assertThat(dto.getChildEmail()).isEqualTo("child@example.com");
        assertThat(dto.isParentConfirmed()).isTrue();
        assertThat(dto.isChildConfirmed()).isFalse();
        // fullyConfirmed == parentConfirmed && childConfirmed
        assertThat(dto.isFullyConfirmed()).isFalse();
    }

    @Test
    void shouldMapDtoToEntity_ignoringParentAndChildRefs() {
        // Given
        UserRelationshipResDto dto = UserRelationshipResDto.builder()
                                                           .id(123L)
                                                           .parentId(11L)
                                                           .parentEmail("p@ex.com")
                                                           .childId(22L)
                                                           .childEmail("c@ex.com")
                                                           .parentConfirmed(false)
                                                           .childConfirmed(true)
                                                           .fullyConfirmed(true)
                                                           .build();

        // When
        UserRelationship entity = mapper.toEntity(dto);

        // Then
        assertThat(entity).isNotNull();
        // id should be mapped
        assertThat(entity.getId()).isEqualTo(123L);
        // parent & child objects are ignored by the mapping
        assertThat(entity.getParent()).isNull();
        assertThat(entity.getChild()).isNull();
        // confirmations are mapped
        assertThat(entity.isParentConfirmed()).isFalse();
        assertThat(entity.isChildConfirmed()).isTrue();
        // fullyConfirmed est calculé dynamiquement, et non stocké
        assertThat(entity.isFullyConfirmed()).isFalse(); // false && true => false
    }
}
