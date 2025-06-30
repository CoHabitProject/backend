package fr.esgi.service.task;

import fr.esgi.domain.DateUtils;
import fr.esgi.domain.dto.task.TaskReqDto;
import fr.esgi.domain.dto.task.TaskResDto;
import fr.esgi.domain.dto.task.TaskStatus;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.persistence.document.TaskDocument;
import fr.esgi.persistence.entity.space.Colocation;
import fr.esgi.persistence.entity.user.User;
import fr.esgi.persistence.repository.space.ColocationRepository;
import fr.esgi.persistence.repository.task.TaskRepository;
import fr.esgi.persistence.repository.user.UserRepository;
import fr.esgi.service.AbstractTest;
import fr.esgi.service.registration.mapper.UserMapper;
import fr.esgi.service.task.mapper.TaskMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DataJpaTest
@Import(TaskServiceTest.TestConfig.class)
@TestPropertySource(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
        }
)
@EnableJpaRepositories(basePackages = "fr.esgi.persistence.repository")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TaskServiceTest extends AbstractTest {

    @TestConfiguration
    @EnableAutoConfiguration(
            exclude = {
                    ServletWebServerFactoryAutoConfiguration.class,
                    ReactiveWebServerFactoryAutoConfiguration.class
            }
    )
    static class TestConfig {
        @Bean
        public TaskMapper taskMapper() {
            return Mappers.getMapper(TaskMapper.class);
        }

        @Bean
        public TaskRepository taskRepository() {
            return mock(TaskRepository.class);
        }

        @Bean
        public UserMapper userMapper() { return Mappers.getMapper(UserMapper.class); }

        @Bean
        public TaskService taskService(
                TaskRepository taskRepository,
                UserRepository userRepository,
                ColocationRepository colocationRepository,
                TaskMapper taskMapper,
                UserMapper userMapper) {
            return new TaskService(taskRepository, userRepository, colocationRepository, taskMapper, userMapper);
        }
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ColocationRepository colocationRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    private User         managerUser;
    private User         roommateUser;
    private User         otherUser;
    private Colocation   colocation;
    private Colocation   otherColocation;
    private TaskDocument existingTask;

    @BeforeEach
    public void initData() {
        // Reset mocks
        reset(taskRepository);

        // Setup users
        managerUser = new User();
        managerUser.setEmail("manager@example.com");
        managerUser.setFirstName("John");
        managerUser.setLastName("Manager");
        managerUser.setKeyCloakSub(TEST_USER_ID);
        managerUser.setBirthDate(LocalDate.of(1990, 1, 1));

        roommateUser = new User();
        roommateUser.setEmail("roommate@example.com");
        roommateUser.setFirstName("Jane");
        roommateUser.setLastName("Roommate");
        roommateUser.setKeyCloakSub("roommate-sub");
        roommateUser.setBirthDate(LocalDate.of(1992, 1, 1));

        otherUser = new User();
        otherUser.setEmail("other@example.com");
        otherUser.setFirstName("Bob");
        otherUser.setLastName("Other");
        otherUser.setKeyCloakSub("other-sub");
        otherUser.setBirthDate(LocalDate.of(1988, 1, 1));

        // Save users first to get IDs
        managerUser  = userRepository.save(managerUser);
        roommateUser = userRepository.save(roommateUser);
        otherUser    = userRepository.save(otherUser);

        // Setup colocations
        colocation = new Colocation("Test Coloc", "Test Address", managerUser);
        colocation.addRoommate(roommateUser);
        colocation = colocationRepository.save(colocation);

        // Manually sync the bidirectional relationship for testing
        managerUser.getColocations()
                   .add(colocation);
        roommateUser.getColocations()
                    .add(colocation);
        userRepository.save(managerUser);
        userRepository.save(roommateUser);

        otherColocation = new Colocation("Other Coloc", "Other Address", otherUser);
        otherColocation = colocationRepository.save(otherColocation);

        // Setup existing task
        existingTask = new TaskDocument();
        existingTask.setId("existing-task-id");
        existingTask.setTitle("Existing Task");
        existingTask.setDescription("Existing Description");
        existingTask.setColocationId(colocation.getId());
        existingTask.setCreatorId(managerUser.getId());
        existingTask.setStatus(TaskDocument.TaskStatus.TODO);
        existingTask.setDueDate(LocalDateTime.now()
                                             .plusDays(7));
    }

    @AfterEach
    public void cleanUp() {
        colocationRepository.deleteAll();
        userRepository.deleteAll();
        this.cleanupSecurityContext();
    }

    @Test
    public void testCreateTask_Success() throws
                                         TechnicalException {
        // Given
        this.initSecurityContextPlaceHolderWithSub(TEST_USER_ID);

        TaskReqDto dto = new TaskReqDto();
        dto.setTitle("Clean Kitchen");
        dto.setDescription("Clean all dishes and surfaces");
        dto.setDueDate(DateUtils.localDateTimeToString(LocalDateTime.now()
                                                                    .plusDays(7)));
        dto.setStatus(TaskStatus.TODO);

        TaskDocument savedTask = new TaskDocument();
        savedTask.setId("new-task-id");
        savedTask.setTitle("Clean Kitchen");
        savedTask.setDescription("Clean all dishes and surfaces");
        savedTask.setCreatorId(managerUser.getId());
        savedTask.setColocationId(colocation.getId());
        savedTask.setStatus(TaskDocument.TaskStatus.TODO);

        when(taskRepository.save(any(TaskDocument.class))).thenReturn(savedTask);

        // When
        TaskResDto result = taskService.createTask(colocation.getId(), dto);

        // Then
        assertNotNull(result);
        assertEquals("Clean Kitchen", result.getTitle());
        assertEquals("Clean all dishes and surfaces", result.getDescription());
        assertEquals(colocation.getId(), result.getColocationId());
        assertEquals(managerUser.getId(), result.getCreatorId());
        assertEquals(TaskStatus.TODO, result.getStatus());
        verify(taskRepository).save(any(TaskDocument.class));
    }

    @Test
    public void testCreateTask_UserNotMember() {
        // Given
        this.initSecurityContextPlaceHolderWithSub("other-sub");

        TaskReqDto dto = new TaskReqDto();
        dto.setTitle("Clean Kitchen");

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> taskService.createTask(colocation.getId(), dto));

        assertEquals(403, exception.getCode());
        assertEquals("Vous n'avez pas accès à cette colocation", exception.getMessage());
        verify(taskRepository, never()).save(any(TaskDocument.class));
    }

    @Test
    public void testCreateTask_ColocationNotFound() {
        // Given
        this.initSecurityContextPlaceHolderWithSub(TEST_USER_ID);

        TaskReqDto dto = new TaskReqDto();
        dto.setTitle("Clean Kitchen");

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> taskService.createTask(999L, dto));

        assertEquals(404, exception.getCode());
        assertEquals("Colocation non trouvée", exception.getMessage());
    }

    @Test
    public void testUpdateTask_Success() throws
                                         TechnicalException {
        // Given
        this.initSecurityContextPlaceHolderWithSub(TEST_USER_ID);

        TaskReqDto updateDto = new TaskReqDto();
        updateDto.setTitle("Updated Title");
        updateDto.setDescription("Updated Description");
        updateDto.setStatus(TaskStatus.IN_PROGRESS);

        TaskDocument updatedTask = new TaskDocument();
        updatedTask.setId("existing-task-id");
        updatedTask.setTitle("Updated Title");
        updatedTask.setDescription("Updated Description");
        updatedTask.setStatus(TaskDocument.TaskStatus.IN_PROGRESS);

        when(taskRepository.findById("existing-task-id")).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(TaskDocument.class))).thenReturn(updatedTask);

        // When
        TaskResDto result = taskService.updateTask("existing-task-id", updateDto);

        // Then
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        verify(taskRepository).findById("existing-task-id");
        verify(taskRepository).save(any(TaskDocument.class));
    }

    @Test
    public void testUpdateTask_TaskNotFound() {
        // Given
        this.initSecurityContextPlaceHolderWithSub(TEST_USER_ID);

        TaskReqDto updateDto = new TaskReqDto();
        updateDto.setTitle("Updated Title");

        when(taskRepository.findById("non-existing-id")).thenReturn(Optional.empty());

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> taskService.updateTask("non-existing-id", updateDto));

        assertEquals(404, exception.getCode());
        assertEquals("Tâche non trouvée", exception.getMessage());
    }

    @Test
    public void testGetTaskById_Success() throws TechnicalException {
        this.initSecurityContextPlaceHolderWithSub(TEST_USER_ID);
        when(taskRepository.findById("existing-task-id"))
                .thenReturn(Optional.of(existingTask));

        TaskResDto result = taskService.getTaskById("existing-task-id");

        assertNotNull(result);
        assertEquals("Existing Task", result.getTitle());
        // NOUVEAU
        assertNotNull(result.getAssignedUsers());
        assertTrue(result.getAssignedUsers().isEmpty());
        verify(taskRepository).findById("existing-task-id");
    }

    @Test
    public void testGetTaskById_NotFound() {
        // Given
        this.initSecurityContextPlaceHolderWithSub(TEST_USER_ID);

        when(taskRepository.findById("non-existing-id")).thenReturn(Optional.empty());

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> taskService.getTaskById("non-existing-id"));

        assertEquals(404, exception.getCode());
        assertEquals("Tâche non trouvée", exception.getMessage());
    }

    @Test
    public void testGetTasksByColocation_Success() throws
                                                   TechnicalException {
        // Given
        this.initSecurityContextPlaceHolderWithSub(TEST_USER_ID);

        List<TaskDocument> tasks = List.of(existingTask);
        when(taskRepository.findByColocationId(colocation.getId())).thenReturn(tasks);

        // When
        List<TaskResDto> result = taskService.getTasksByColocation(colocation.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Existing Task",
                     result.get(0)
                           .getTitle());
        verify(taskRepository).findByColocationId(colocation.getId());
    }

    @Test
    public void testGetTasksByColocation_UserNotMember() {
        // Given
        this.initSecurityContextPlaceHolderWithSub("other-sub");

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> taskService.getTasksByColocation(colocation.getId()));

        assertEquals(403, exception.getCode());
        assertEquals("Vous n'avez pas accès à cette colocation", exception.getMessage());
    }

    @Test
    public void testGetUserTasks_Success() throws
                                           TechnicalException {
        // Given
        this.initSecurityContextPlaceHolderWithSub(TEST_USER_ID);

        List<TaskDocument> userTasks = List.of(existingTask);
        when(taskRepository.findByAssignedToUserKeycloakSubs(TEST_USER_ID)).thenReturn(userTasks);

        // When
        List<TaskResDto> result = taskService.getUserTasks();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Existing Task",
                     result.get(0)
                           .getTitle());
        verify(taskRepository).findByAssignedToUserKeycloakSubs(TEST_USER_ID);
    }

    @Test
    public void testAssignTask_Success() throws
                                         TechnicalException {
        // Given
        this.initSecurityContextPlaceHolderWithSub(TEST_USER_ID);

        TaskDocument updatedTask = new TaskDocument();
        updatedTask.setId("existing-task-id");
        updatedTask.setTitle("Existing Task");

        when(taskRepository.findById("existing-task-id")).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(TaskDocument.class))).thenReturn(updatedTask);

        // When
        TaskResDto result = taskService.assignTask("existing-task-id", roommateUser.getId());

        // Then
        assertNotNull(result);
        verify(taskRepository).findById("existing-task-id");
        verify(taskRepository).save(any(TaskDocument.class));
    }

    @Test
    public void testAssignTask_TaskNotFound() {
        // Given
        this.initSecurityContextPlaceHolderWithSub(TEST_USER_ID);

        when(taskRepository.findById("non-existing-id")).thenReturn(Optional.empty());

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> taskService.assignTask("non-existing-id", roommateUser.getId()));

        assertEquals(404, exception.getCode());
        assertEquals("Tâche non trouvée", exception.getMessage());
    }

    @Test
    public void testAssignTask_AssigneeNotFound() {
        // Given
        this.initSecurityContextPlaceHolderWithSub(TEST_USER_ID);

        when(taskRepository.findById("existing-task-id")).thenReturn(Optional.of(existingTask));

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> taskService.assignTask("existing-task-id", 999L));

        assertEquals(404, exception.getCode());
        assertEquals("Utilisateur assigné non trouvé", exception.getMessage());
    }

    @Test
    public void testCompleteTask_Success() throws
                                           TechnicalException {
        // Given
        this.initSecurityContextPlaceHolderWithSub(TEST_USER_ID);

        TaskDocument completedTask = new TaskDocument();
        completedTask.setId("existing-task-id");
        completedTask.setStatus(TaskDocument.TaskStatus.COMPLETED);

        when(taskRepository.findById("existing-task-id")).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(TaskDocument.class))).thenReturn(completedTask);

        // When
        TaskResDto result = taskService.completeTask("existing-task-id");

        // Then
        assertNotNull(result);
        assertEquals(TaskStatus.COMPLETED, result.getStatus());
        verify(taskRepository).findById("existing-task-id");
        verify(taskRepository).save(any(TaskDocument.class));
    }

    @Test
    public void testCompleteTask_TaskNotFound() {
        // Given
        this.initSecurityContextPlaceHolderWithSub(TEST_USER_ID);

        when(taskRepository.findById("non-existing-id")).thenReturn(Optional.empty());

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> taskService.completeTask("non-existing-id"));

        assertEquals(404, exception.getCode());
        assertEquals("Tâche non trouvée", exception.getMessage());
    }

    @Test
    public void testDeleteTask_Success() throws
                                         TechnicalException {
        // Given
        this.initSecurityContextPlaceHolderWithSub(TEST_USER_ID);

        when(taskRepository.findById("existing-task-id")).thenReturn(Optional.of(existingTask));

        // When
        assertDoesNotThrow(() -> taskService.deleteTask("existing-task-id"));

        // Then
        verify(taskRepository).findById("existing-task-id");
        verify(taskRepository).delete(existingTask);
    }

    @Test
    public void testDeleteTask_TaskNotFound() {
        // Given
        this.initSecurityContextPlaceHolderWithSub(TEST_USER_ID);

        when(taskRepository.findById("non-existing-id")).thenReturn(Optional.empty());

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> taskService.deleteTask("non-existing-id"));

        assertEquals(404, exception.getCode());
        assertEquals("Tâche non trouvée", exception.getMessage());
    }

    @Test
    public void testDeleteTask_UserNotAuthorized() {
        // Given
        this.initSecurityContextPlaceHolderWithSub("other-sub");

        when(taskRepository.findById("existing-task-id")).thenReturn(Optional.of(existingTask));

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> taskService.deleteTask("existing-task-id"));

        assertEquals(403, exception.getCode());
        assertEquals("Vous n'avez pas accès à cette tâche", exception.getMessage());
    }

    @Test
    public void testCreateTask_AsRoommate() throws
                                            TechnicalException {
        // Given
        this.initSecurityContextPlaceHolderWithSub("roommate-sub");

        TaskReqDto dto = new TaskReqDto();
        dto.setTitle("Roommate Task");
        dto.setDescription("Task created by roommate");
        dto.setStatus(TaskStatus.TODO);

        TaskDocument savedTask = new TaskDocument();
        savedTask.setId("roommate-task-id");
        savedTask.setTitle("Roommate Task");
        savedTask.setDescription("Task created by roommate");
        savedTask.setCreatorId(roommateUser.getId());
        savedTask.setColocationId(colocation.getId());
        savedTask.setStatus(TaskDocument.TaskStatus.TODO);

        when(taskRepository.save(any(TaskDocument.class))).thenReturn(savedTask);

        // When
        TaskResDto result = taskService.createTask(colocation.getId(), dto);

        // Then
        assertNotNull(result);
        assertEquals("Roommate Task", result.getTitle());
        assertEquals(roommateUser.getId(), result.getCreatorId());
        verify(taskRepository).save(any(TaskDocument.class));
    }
}
