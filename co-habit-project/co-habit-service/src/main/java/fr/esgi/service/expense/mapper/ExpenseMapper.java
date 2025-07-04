package fr.esgi.service.expense.mapper;

import fr.esgi.domain.dto.expense.ExpenseParticipantResDto;
import fr.esgi.domain.dto.expense.ExpenseReqDto;
import fr.esgi.domain.dto.expense.ExpenseResDto;
import fr.esgi.domain.dto.user.UserProfileResDto;
import fr.esgi.domain.DateUtils;
import fr.esgi.persistence.entity.expense.Expense;
import fr.esgi.persistence.entity.expense.ExpenseParticipant;
import fr.esgi.persistence.entity.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    /**
     * Maps ExpenseReqDto to Expense entity for creation
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "settledAt", ignore = true)
    @Mapping(target = "payer", ignore = true)
    @Mapping(target = "space", ignore = true)
    @Mapping(target = "participants", ignore = true)
    @Mapping(target = "settled", ignore = true)
    Expense mapDtoToExpense(ExpenseReqDto dto);

    /**
     * Maps Expense entity to ExpenseResDto
     */
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "settledAt", source = "settledAt", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "payer", source = "payer")
    @Mapping(target = "spaceId", source = "space.id")
    @Mapping(target = "spaceName", source = "space.name")
    @Mapping(target = "participants", source = "participants")
    ExpenseResDto mapExpenseToResDto(Expense expense);

    /**
     * Maps list of Expense entities to list of ExpenseResDto
     */
    List<ExpenseResDto> mapExpensesToResDtos(List<Expense> expenses);

    /**
     * Maps ExpenseParticipant entity to ExpenseParticipantResDto
     */
    @Mapping(target = "user", source = "user")
    @Mapping(target = "validatedAt", source = "validatedAt", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "confirmedByCreatorAt", source = "confirmedByCreatorAt", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "fullySettled", source = ".", qualifiedByName = "isFullySettled")
    ExpenseParticipantResDto mapExpenseParticipantToResDto(ExpenseParticipant participant);

    /**
     * Maps list of ExpenseParticipant entities to list of ExpenseParticipantResDto
     */
    List<ExpenseParticipantResDto> mapExpenseParticipantsToResDtos(List<ExpenseParticipant> participants);

    /**
     * Maps User entity to UserProfileResDto
     */
    @Mapping(target = "birthDate", source = "birthDate", qualifiedByName = "localDateToString")
    UserProfileResDto mapUserToUserProfileResDto(User user);

    @Named("localDateTimeToString")
    default String localDateTimeToString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toString();
    }

    @Named("localDateToString")
    default String localDateToString(java.time.LocalDate date) {
        if (date == null) {
            return null;
        }
        return DateUtils.localDateToString(date);
    }

    @Named("isFullySettled")
    default boolean isFullySettled(ExpenseParticipant participant) {
        return participant.isFullySettled();
    }
}
