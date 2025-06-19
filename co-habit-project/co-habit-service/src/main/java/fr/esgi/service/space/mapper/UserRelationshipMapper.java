package fr.esgi.service.space.mapper;

import fr.esgi.domain.dto.user.UserRelationshipResDto;
import fr.esgi.persistence.entity.user.UserRelationship;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserRelationshipMapper {

    UserRelationshipMapper INSTANCE = Mappers.getMapper(UserRelationshipMapper.class);

    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "parent.email", target = "parentEmail")
    @Mapping(source = "child.id", target = "childId")
    @Mapping(source = "child.email", target = "childEmail")
    @Mapping(source = "fullyConfirmed", target = "fullyConfirmed")
    UserRelationshipResDto toDto(UserRelationship entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "child", ignore = true)
    UserRelationship toEntity(UserRelationshipResDto dto);
}
