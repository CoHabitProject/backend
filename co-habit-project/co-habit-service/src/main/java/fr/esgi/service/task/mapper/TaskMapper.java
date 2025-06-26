package fr.esgi.service.task.mapper;

import fr.esgi.domain.dto.task.TaskReqDto;
import fr.esgi.domain.dto.task.TaskResDto;
import fr.esgi.domain.dto.user.UserProfileResDto;
import fr.esgi.persistence.document.TaskDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    
    TaskMapper INSTANCE = Mappers.getMapper(TaskMapper.class);
    
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "completedAt", source = "completedAt", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "dueDate", source = "dueDate")
    @Mapping(target = "assignedUsers", ignore = true)
    TaskResDto toTaskResDto(TaskDocument taskDocument);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "userKeycloakSub", ignore = true)
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "colocationId", ignore = true)
    @Mapping(target = "colocationName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "creatorId", ignore = true)
    @Mapping(target = "assignedToUserKeycloakSubs", ignore = true)
    @Mapping(target = "dueDate", source = "dueDate")
    TaskDocument toTaskDocument(TaskReqDto taskReqDto);
    
    @Mapping(target = "assignedUsers", source = "assignedUsers")
    @Mapping(target = "dueDate", source = "taskDocument.dueDate", qualifiedByName = "localDateToLocalDateTime")
    TaskResDto toTaskResDtoWithUsers(TaskDocument taskDocument, List<UserProfileResDto> assignedUsers);
    
    @Named("localDateTimeToString")
    default String localDateTimeToString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    @Named("localDateToLocalDateTime")
    default LocalDateTime localDateToLocalDateTime(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }
    
    @Named("localDateTimeToLocalDate")
    default LocalDate localDateTimeToLocalDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate();
    }
    
    // Enum mappings
    default fr.esgi.domain.dto.task.TaskStatus mapStatus(TaskDocument.TaskStatus status) {
        if (status == null) return null;
        return fr.esgi.domain.dto.task.TaskStatus.valueOf(status.name());
    }
    
    default TaskDocument.TaskStatus mapStatus(fr.esgi.domain.dto.task.TaskStatus status) {
        if (status == null) return null;
        return TaskDocument.TaskStatus.valueOf(status.name());
    }
    
    default fr.esgi.domain.dto.task.TaskPriority mapPriority(TaskDocument.TaskPriority priority) {
        if (priority == null) return null;
        return fr.esgi.domain.dto.task.TaskPriority.valueOf(priority.name());
    }
    
    default TaskDocument.TaskPriority mapPriority(fr.esgi.domain.dto.task.TaskPriority priority) {
        if (priority == null) return null;
        return TaskDocument.TaskPriority.valueOf(priority.name());
    }
}
