package fr.esgi.service.space.mapper;

import fr.esgi.domain.dto.space.ColocationReqDto;
import fr.esgi.domain.dto.space.ColocationResDto;
import fr.esgi.domain.dto.user.UserProfileResDto;
import fr.esgi.domain.util.DateUtils;
import fr.esgi.persistence.entity.space.Colocation;
import fr.esgi.persistence.entity.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ColocationMapper {

    /**
     * Maps ColocationReqDto to Colocation entity for creation
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "invitationCode", ignore = true)
    @Mapping(target = "maxRoommates", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "roommates", ignore = true)
    Colocation mapDtoToColocation(ColocationReqDto dto);

    /**
     * Maps Colocation entity to ColocationResDto
     */
    @Mapping(target = "numberOfPeople", expression = "java(colocation.getRoommates().size())")
    @Mapping(target = "dateEntree", source = "createdAt", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "localDateTimeToISOString")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "localDateTimeToISOString")
    @Mapping(target = "invitationCode", source = "invitationCode")
    @Mapping(target = "manager", source = "manager")
    @Mapping(target = "users", source = "roommates")
    ColocationResDto mapColocationToResDto(Colocation colocation);

    /**
     * Maps list of Colocation entities to list of ColocationResDto
     */
    List<ColocationResDto> mapColocationsToResDtos(List<Colocation> colocations);

    /**
     * Updates existing Colocation entity with data from ColocationReqDto
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "invitationCode", ignore = true)
    @Mapping(target = "maxRoommates", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "roommates", ignore = true)
    void updateColocationFromDto(ColocationReqDto dto, @MappingTarget Colocation colocation);

    @Named("localDateTimeToString")
    default String localDateTimeToString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return DateUtils.localDateToString(dateTime.toLocalDate());
    }

    @Named("localDateTimeToISOString")
    default String localDateTimeToISOString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toString();
    }

    @Named("usersToUserProfileDtos")
    default List<UserProfileResDto> usersToUserProfileDtos(java.util.Set<User> users) {
        if (users == null) {
            return List.of();
        }
        return users.stream()
                .map(user -> {
                    UserProfileResDto dto = new UserProfileResDto();
                    dto.setFirstName(user.getFirstName());
                    dto.setLastName(user.getLastName());
                    dto.setEmail(user.getEmail());
                    dto.setBirthDate(user.getBirthDate() != null ? DateUtils.localDateToString(user.getBirthDate()) : null);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    UserProfileResDto mapUserToUserProfileResDto(User user);
}
