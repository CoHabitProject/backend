package fr.esgi.service.task.mapper;

import fr.esgi.domain.dto.task.TaskPriority;
import fr.esgi.domain.dto.task.TaskReqDto;
import fr.esgi.domain.dto.task.TaskResDto;
import fr.esgi.domain.dto.task.TaskStatus;
import fr.esgi.domain.dto.user.UserProfileResDto;
import fr.esgi.persistence.document.TaskDocument;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig
class TaskMapperTest {

    private final TaskMapper taskMapper = Mappers.getMapper(TaskMapper.class);

    @Test
    void shouldMapTaskDocumentToTaskResDto() {
        // Given
        TaskDocument taskDocument = TaskDocument.builder()
                                                .id("task-123")
                                                .userId(1L)
                                                .userName("John Doe")
                                                .colocationId(10L)
                                                .colocationName("Test Colocation")
                                                .title("Test Task")
                                                .description("Test Description")
                                                .status(TaskDocument.TaskStatus.IN_PROGRESS)
                                                .priority(TaskDocument.TaskPriority.HIGH)
                                                .createdAt(LocalDateTime.of(2025, 1, 15, 10, 30))
                                                .dueDate(LocalDateTime.of(2025, 2, 1, 0, 0))
                                                .completedAt(LocalDateTime.of(2025, 1, 20, 14, 45))
                                                .creatorId(2L)
                                                .assignedUserIds(Set.of(1L, 2L))
                                                .tags(Set.of("urgent", "cleaning"))
                                                .build();

        // When
        TaskResDto result = taskMapper.toTaskResDto(taskDocument);

        // Then
        assertNotNull(result);
        assertEquals("task-123", result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals("John Doe", result.getUserName());
        assertEquals(10L, result.getColocationId());
        assertEquals("Test Colocation", result.getColocationName());
        assertEquals("Test Task", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        assertEquals(TaskPriority.HIGH, result.getPriority());
        assertEquals("2025-01-15T10:30:00", result.getCreatedAt());
        assertEquals(LocalDateTime.of(2025, 02, 01, 0, 0), result.getDueDate());
        assertEquals("2025-01-20T14:45:00", result.getCompletedAt());
        assertEquals(2L, result.getCreatorId());
        assertEquals(Set.of(1L, 2L), result.getAssignedUserIds());
        assertEquals(Set.of("urgent", "cleaning"), result.getTags());
        assertNull(result.getAssignedUsers());
    }

    @Test
    void shouldMapTaskReqDtoToTaskDocument() {
        // Given
        TaskReqDto taskReqDto = new TaskReqDto();
        taskReqDto.setTitle("New Task");
        taskReqDto.setDescription("New Description");
        taskReqDto.setStatus(TaskStatus.TODO);
        taskReqDto.setPriority(TaskPriority.MEDIUM);
        taskReqDto.setDueDate(LocalDateTime.of(2025, 3, 15, 0, 0));
        taskReqDto.setAssignedUserIds(Set.of(3L, 4L));
        taskReqDto.setTags(Set.of("shopping", "weekly"));

        // When
        TaskDocument result = taskMapper.toTaskDocument(taskReqDto);

        // Then
        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getUserId());
        assertNull(result.getUserKeycloakSub());
        assertNull(result.getUserName());
        assertNull(result.getColocationId());
        assertNull(result.getColocationName());
        assertEquals("New Task", result.getTitle());
        assertEquals("New Description", result.getDescription());
        assertEquals(TaskDocument.TaskStatus.TODO, result.getStatus());
        assertEquals(TaskDocument.TaskPriority.MEDIUM, result.getPriority());
        assertNull(result.getCreatedAt());
        assertEquals(LocalDateTime.of(2025, 3, 15, 0, 0), result.getDueDate());
        assertNull(result.getCompletedAt());
        assertNull(result.getCreatorId());
        assertEquals(Set.of(3L, 4L), result.getAssignedUserIds());
        assertNull(result.getAssignedToUserKeycloakSubs());
        assertEquals(Set.of("shopping", "weekly"), result.getTags());
    }

    @Test
    void shouldMapTaskDocumentWithAssignedUsers() {
        // Given
        TaskDocument taskDocument = TaskDocument.builder()
                                                .id("task-456")
                                                .title("Task with Users")
                                                .status(TaskDocument.TaskStatus.COMPLETED)
                                                .priority(TaskDocument.TaskPriority.LOW)
                                                .assignedUserIds(Set.of(5L, 6L))
                                                .createdAt(LocalDateTime.of(2025, 1, 10, 9, 0))
                                                .build();

        List<UserProfileResDto> assignedUsers = Arrays.asList(
                createUserProfile(5L, "Alice"),
                createUserProfile(6L, "Bob")
        );

        // When
        TaskResDto result = taskMapper.toTaskResDtoWithUsers(taskDocument, assignedUsers);

        // Then
        assertNotNull(result);
        assertEquals("task-456", result.getId());
        assertEquals("Task with Users", result.getTitle());
        assertEquals(TaskStatus.COMPLETED, result.getStatus());
        assertEquals(TaskPriority.LOW, result.getPriority());
        assertEquals(Set.of(5L, 6L), result.getAssignedUserIds());
        assertEquals(2,
                     result.getAssignedUsers()
                           .size());
        assertEquals(assignedUsers, result.getAssignedUsers());
        assertEquals("2025-01-10T09:00:00", result.getCreatedAt());
    }

    @Test
    void shouldHandleNullValues() {
        // Given
        TaskDocument taskDocumentWithNulls = TaskDocument.builder()
                                                         .id("null-test")
                                                         .title("Test")
                                                         .build();

        // When
        TaskResDto result = taskMapper.toTaskResDto(taskDocumentWithNulls);

        // Then
        assertNotNull(result);
        assertEquals("null-test", result.getId());
        assertEquals("Test", result.getTitle());
        assertNull(result.getDescription());
        assertNull(result.getStatus());
        assertNull(result.getPriority());
        assertNull(result.getCreatedAt());
        assertNull(result.getCompletedAt());
        assertNull(result.getDueDate());
    }

    @Test
    void shouldMapEnumsCorrectly() {
        // Test all TaskStatus enum values
        for (TaskDocument.TaskStatus docStatus : TaskDocument.TaskStatus.values()) {
            TaskStatus dtoStatus = taskMapper.mapStatus(docStatus);
            assertNotNull(dtoStatus);
            assertEquals(docStatus.name(), dtoStatus.name());

            TaskDocument.TaskStatus backToDocStatus = taskMapper.mapStatus(dtoStatus);
            assertEquals(docStatus, backToDocStatus);
        }

        // Test all TaskPriority enum values
        for (TaskDocument.TaskPriority docPriority : TaskDocument.TaskPriority.values()) {
            TaskPriority dtoPriority = taskMapper.mapPriority(docPriority);
            assertNotNull(dtoPriority);
            assertEquals(docPriority.name(), dtoPriority.name());

            TaskDocument.TaskPriority backToDocPriority = taskMapper.mapPriority(dtoPriority);
            assertEquals(docPriority, backToDocPriority);
        }
    }

    @Test
    void shouldVerifyFrenchLabels() {
        // Test TaskStatus French labels
        assertEquals("À faire", TaskStatus.TODO.getFrenchLabel());
        assertEquals("En cours", TaskStatus.IN_PROGRESS.getFrenchLabel());
        assertEquals("Terminée", TaskStatus.COMPLETED.getFrenchLabel());
        assertEquals("Annulée", TaskStatus.CANCELLED.getFrenchLabel());

        // Test TaskPriority French labels
        assertEquals("Faible", TaskPriority.LOW.getFrenchLabel());
        assertEquals("Moyenne", TaskPriority.MEDIUM.getFrenchLabel());
        assertEquals("Élevée", TaskPriority.HIGH.getFrenchLabel());
        assertEquals("Urgent", TaskPriority.URGENT.getFrenchLabel());
    }

    @Test
    void shouldHandleNullEnums() {
        assertNull(taskMapper.mapStatus((TaskDocument.TaskStatus) null));
        assertNull(taskMapper.mapStatus((TaskStatus) null));
        assertNull(taskMapper.mapPriority((TaskDocument.TaskPriority) null));
        assertNull(taskMapper.mapPriority((TaskPriority) null));
    }

    @Test
    void shouldFormatDateTimeCorrectly() {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2025, 6, 25, 8, 20, 41);

        // When
        String formatted = taskMapper.localDateTimeToString(dateTime);

        // Then
        assertEquals("2025-06-25T08:20:41", formatted);
    }

    @Test
    void shouldHandleNullDateTime() {
        assertNull(taskMapper.localDateTimeToString(null));
    }

    @Test
    void shouldMapCompleteTaskDocument() {
        // Given
        TaskDocument fullTaskDocument = TaskDocument.builder()
                                                    .id("complete-task")
                                                    .userId(100L)
                                                    .userKeycloakSub("keycloak-sub-123")
                                                    .userName("Complete User")
                                                    .colocationId(200L)
                                                    .colocationName("Complete Colocation")
                                                    .title("Complete Task")
                                                    .description("Complete Description")
                                                    .status(TaskDocument.TaskStatus.CANCELLED)
                                                    .priority(TaskDocument.TaskPriority.URGENT)
                                                    .createdAt(LocalDateTime.of(2025, 1, 1, 12, 0))
                                                    .dueDate(LocalDateTime.of(2025, 12, 31, 0, 0))
                                                    .completedAt(LocalDateTime.of(2025, 1, 2, 15, 30))
                                                    .creatorId(300L)
                                                    .assignedUserIds(Set.of(100L, 200L, 300L))
                                                    .assignedToUserKeycloakSubs(Set.of("sub1", "sub2", "sub3"))
                                                    .tags(Set.of("important", "final", "test"))
                                                    .build();

        // When
        TaskResDto result = taskMapper.toTaskResDto(fullTaskDocument);

        // Then
        assertNotNull(result);
        assertEquals("complete-task", result.getId());
        assertEquals(100L, result.getUserId());
        assertEquals("Complete User", result.getUserName());
        assertEquals(200L, result.getColocationId());
        assertEquals("Complete Colocation", result.getColocationName());
        assertEquals("Complete Task", result.getTitle());
        assertEquals("Complete Description", result.getDescription());
        assertEquals(TaskStatus.CANCELLED, result.getStatus());
        assertEquals(TaskPriority.URGENT, result.getPriority());
        assertEquals("2025-01-01T12:00:00", result.getCreatedAt());
        assertEquals(LocalDateTime.of(2025, 12, 31, 0, 0), result.getDueDate());
        assertEquals("2025-01-02T15:30:00", result.getCompletedAt());
        assertEquals(300L, result.getCreatorId());
        assertEquals(Set.of(100L, 200L, 300L), result.getAssignedUserIds());
        assertEquals(Set.of("important", "final", "test"), result.getTags());
    }

    private UserProfileResDto createUserProfile(Long id, String name) {
        UserProfileResDto user = new UserProfileResDto();
        user.setId(id);
        user.setFirstName(name);
        return user;
    }
}
