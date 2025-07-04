package fr.esgi.rest.interne;

import fr.esgi.domain.dto.task.TaskReqDto;
import fr.esgi.domain.dto.task.TaskResDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.service.task.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskRestTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskRest taskRest;

    private Long idCollocation;
    private String taskId;
    private Long userId;
    private TaskReqDto    testReqDto;
    private TaskResDto    testResDto;
    private List<TaskResDto> testResList;

    @BeforeEach
    void setUp() {
        idCollocation = 1L;
        taskId        = "task-123";
        userId        = 42L;

        testReqDto = TaskReqDto.builder()
                .title("Faire les courses")
                .description("Acheter du lait et du pain")
                .dueDate("2025-07-05")
                .build();

        testResDto = TaskResDto.builder()
                .id(taskId)
                .title("Faire les courses")
                .description("Acheter du lait et du pain")
                .dueDate("2025-07-05")
                .assignedUserIds(null)
                .completedAt(null)
                .build();

        testResList = List.of(testResDto);
    }

    @Test
    void createTask_ShouldReturnCreatedTask() throws TechnicalException {
        when(taskService.createTask(eq(idCollocation), any(TaskReqDto.class)))
                .thenReturn(testResDto);

        TaskResDto result = taskRest.createTask(idCollocation, testReqDto);

        assertThat(result).isEqualTo(testResDto);
        verify(taskService).createTask(idCollocation, testReqDto);
    }

    @Test
    void getTasksByColocation_ShouldReturnList() throws TechnicalException {
        when(taskService.getTasksByColocation(eq(idCollocation)))
                .thenReturn(testResList);

        List<TaskResDto> result = taskRest.getTasksByColocation(idCollocation);

        assertThat(result).isEqualTo(testResList);
        assertThat(result).hasSize(1);
        verify(taskService).getTasksByColocation(idCollocation);
    }

    @Test
    void getTaskById_ShouldReturnTask() throws TechnicalException {
        when(taskService.getTaskById(eq(taskId)))
                .thenReturn(testResDto);

        TaskResDto result = taskRest.getTaskById(idCollocation, taskId);

        assertThat(result).isEqualTo(testResDto);
        verify(taskService).getTaskById(taskId);
    }

    @Test
    void updateTask_ShouldReturnUpdatedTask() throws TechnicalException {
        when(taskService.updateTask(eq(taskId), any(TaskReqDto.class)))
                .thenReturn(testResDto);

        TaskResDto result = taskRest.updateTask(idCollocation, taskId, testReqDto);

        assertThat(result).isEqualTo(testResDto);
        verify(taskService).updateTask(taskId, testReqDto);
    }

    @Test
    void assignTask_ShouldReturnAssignedTask() throws TechnicalException {
        TaskResDto assigned = TaskResDto.builder()
                .id(taskId)
                .title("Faire les courses")
                .assignedUserIds(Collections.singleton(userId))
                .build();

        when(taskService.assignTask(eq(taskId), eq(userId)))
                .thenReturn(assigned);

        TaskResDto result = taskRest.assignTask(idCollocation, taskId, userId);

        assertThat(result).isEqualTo(assigned);
        verify(taskService).assignTask(taskId, userId);
    }

    @Test
    void completeTask_ShouldReturnCompletedTask() throws TechnicalException {
        TaskResDto done = TaskResDto.builder()
                .id(taskId)
                .completedAt("2025-06-25T08:20:41.678Z")
                .build();

        when(taskService.completeTask(eq(taskId)))
                .thenReturn(done);

        TaskResDto result = taskRest.completeTask(idCollocation, taskId);

        assertThat(result).isEqualTo(done);
        verify(taskService).completeTask(taskId);
    }

    @Test
    void deleteTask_ShouldCallService() throws TechnicalException {
        // no return value
        taskRest.deleteTask(idCollocation, taskId);

        verify(taskService).deleteTask(taskId);
    }

    @Test
    void getUserTasks_ShouldReturnUserTasks() throws TechnicalException {
        when(taskService.getUserTasks())
                .thenReturn(testResList);

        List<TaskResDto> result = taskRest.getUserTasks(idCollocation);

        assertThat(result).isEqualTo(testResList);
        verify(taskService).getUserTasks();
    }

    @Test
    void createTask_ShouldPropagateException() throws TechnicalException {
        when(taskService.createTask(eq(idCollocation), any(TaskReqDto.class)))
                .thenThrow(new TechnicalException(400, "Données invalides"));

        TechnicalException ex = assertThrows(
                TechnicalException.class,
                () -> taskRest.createTask(idCollocation, testReqDto)
        );
        assertThat(ex.getCode()).isEqualTo(400);
        assertThat(ex.getMessage()).contains("Données invalides");
    }

    @Test
    void getRecentTasks_ShouldReturnRecentTasks() throws TechnicalException {
        // Prepare
        List<TaskResDto> recentTasks = List.of(
            TaskResDto.builder().id("r1").title("Tâche 1").build(),
            TaskResDto.builder().id("r2").title("Tâche 2").build()
        );

        when(taskService.getMostRecentTasks(eq(idCollocation)))
            .thenReturn(recentTasks);

        // When
        List<TaskResDto> result = taskRest.getRecentTasks(idCollocation);

        // Assertions
        assertThat(result).isEqualTo(recentTasks);
        assertThat(result).hasSize(2)
                          .extracting(TaskResDto::getId)
                          .containsExactly("r1", "r2");

        // Verify
        verify(taskService).getMostRecentTasks(idCollocation);
    }

}
