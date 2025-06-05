package fr.esgi.service.mapper;

import fr.esgi.domain.dto.auth.RegisterReqDto;
import fr.esgi.domain.dto.user.UserProfileResDto;
import fr.esgi.domain.util.DateUtils;
import fr.esgi.persistence.entity.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Maps RegisterReqDto to User entity for database persistence
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keyCloakSub", source = "userKeycloakId")
    @Mapping(target = "birthDate", source = "dto.birthDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "parents", ignore = true)
    @Mapping(target = "managedColocations", ignore = true)
    @Mapping(target = "colocations", ignore = true)
    User mapDtoToUser(String userKeycloakId, RegisterReqDto dto);

    /**
     * Maps User entity to UserProfileResDto for profile responses
     */
    @Mapping(target = "birthDate", source = "birthDate", qualifiedByName = "localDateToString")
    UserProfileResDto mapUserToProfileDto(User user);

    /**
     * Maps list of User entities to list of UserProfileResDto
     */
    List<UserProfileResDto> mapUsersToProfileDtos(List<User> users);

    /**
     * Updates existing User entity with data from RegisterReqDto
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keyCloakSub", ignore = true)
    @Mapping(target = "birthDate", source = "birthDate", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "parents", ignore = true)
    @Mapping(target = "managedColocations", ignore = true)
    @Mapping(target = "colocations", ignore = true)
    void updateUserFromDto(RegisterReqDto dto, @org.mapstruct.MappingTarget User user);

    @Named("stringToLocalDate")
    default LocalDate stringToLocalDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return DateUtils.stringToLocalDate(dateString);
        } catch (Exception e) {
            // Log error or handle as needed
            return null;
        }
    }

    @Named("localDateToString")
    default String localDateToString(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return DateUtils.localDateToString(localDate);
    }
}
