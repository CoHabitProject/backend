package fr.esgi.service.task;

import fr.esgi.domain.dto.task.TaskReqDto;
import fr.esgi.domain.dto.task.TaskResDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.persistence.document.TaskDocument;
import fr.esgi.persistence.entity.space.Colocation;
import fr.esgi.persistence.entity.user.User;
import fr.esgi.persistence.repository.space.ColocationRepository;
import fr.esgi.persistence.repository.task.TaskRepository;
import fr.esgi.persistence.repository.user.UserRepository;
import fr.esgi.service.AbstractService;
import fr.esgi.service.task.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService extends AbstractService {

    private final TaskRepository       taskRepository;
    private final UserRepository       userRepository;
    private final ColocationRepository colocationRepository;
    private final TaskMapper           taskMapper;

    /**
     * Creates a new task in a colocation
     */
    public TaskResDto createTask(Long colocationId,
                                 TaskReqDto dto) throws
                                                 TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        Colocation colocation = colocationRepository.findById(colocationId)
                                                    .orElseThrow(() -> new TechnicalException(404, "Colocation non trouvée"));

        if (!colocation.isRoommate(user)) {
            throw new TechnicalException(403, "Vous n'avez pas accès à cette colocation");
        }

        TaskDocument task = taskMapper.toTaskDocument(dto);
        task.setCreatorId(user.getId());
        task.setColocationId(colocation.getId());


        TaskDocument savedTask = taskRepository.save(task);
        return taskMapper.toTaskResDto(savedTask);
    }

    /**
     * Updates an existing task
     */
    public TaskResDto updateTask(String taskId, TaskReqDto dto) throws
                                                                TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        TaskDocument task = taskRepository.findById(taskId)
                                          .orElseThrow(() -> new TechnicalException(404, "Tâche non trouvée"));

        // Only collocation member can update task
        if (!task.isRoommate(
                user.getColocations()
                    .stream()
                    .map(Colocation::getId)
                    .collect(Collectors.toSet())
        )) {
            throw new TechnicalException(403, "Vous n'avez pas accès à cette tâche");
        }


        TaskDocument document    = taskMapper.toTaskDocument(dto);
        TaskDocument updatedTask = taskRepository.save(document);
        return taskMapper.toTaskResDto(updatedTask);
    }

    /**
     * Gets a task by ID
     */
    @Transactional(readOnly = true)
    public TaskResDto getTaskById(String taskId) throws
                                                 TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        TaskDocument task = taskRepository.findById(taskId)
                                          .orElseThrow(() -> new TechnicalException(404, "Tâche non trouvée"));

        if (!task.isRoommate(
                user.getColocations()
                    .stream()
                    .map(Colocation::getId)
                    .collect(Collectors.toSet())
        )) {
            throw new TechnicalException(403, "Vous n'avez pas accès à cette tâche");
        }

        return taskMapper.toTaskResDto(task);
    }

    /**
     * Gets all tasks for a colocation
     */
    @Transactional(readOnly = true)
    public List<TaskResDto> getTasksByColocation(Long colocationId) throws
                                                                    TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        Colocation colocation = colocationRepository.findById(colocationId)
                                                    .orElseThrow(() -> new TechnicalException(404, "Colocation non trouvée"));

        if (!colocation.isRoommate(user)) {
            throw new TechnicalException(403, "Vous n'avez pas accès à cette colocation");
        }

        List<TaskDocument> tasks = taskRepository.findByColocationId(colocationId);
        return tasks.stream()
                    .map(taskMapper::toTaskResDto)
                    .toList();
    }

    /**
     * Gets all tasks assigned to the authenticated user
     */
    @Transactional(readOnly = true)
    public List<TaskResDto> getUserTasks() throws
                                           TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        List<TaskDocument> tasks = taskRepository.findByAssignedToUserKeycloakSubs(this.getUserSub());
        return tasks.stream()
                    .map(taskMapper::toTaskResDto)
                    .toList();
    }

    /**
     * Assigns a task to a user
     */
    public TaskResDto assignTask(String taskId, Long userId) throws
                                                             TechnicalException {
        User currentUser = userRepository.findByKeyCloakSub(this.getUserSub())
                                         .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        TaskDocument task = taskRepository.findById(taskId)
                                          .orElseThrow(() -> new TechnicalException(404, "Tâche non trouvée"));

        User assignee = userRepository.findById(userId)
                                      .orElseThrow(() -> new TechnicalException(404, "Utilisateur assigné non trouvé"));

        if (!task.isRoommate(
                currentUser.getColocations()
                           .stream()
                           .map(Colocation::getId)
                           .collect(Collectors.toSet())
        )) {
            throw new TechnicalException(403, "Vous n'avez pas accès à cette tâche");
        }

        if (!task.isRoommate(
                currentUser.getColocations()
                           .stream()
                           .map(Colocation::getId)
                           .collect(Collectors.toSet())
        )) {
            throw new TechnicalException(403, "L'utilisateur assigné n'est pas membre de cette colocation");
        }

        task.addAssignedUser(assignee);
        TaskDocument updatedTask = taskRepository.save(task);
        return taskMapper.toTaskResDto(updatedTask);
    }

    /**
     * Marks a task as completed
     */
    public TaskResDto completeTask(String taskId) throws
                                                  TechnicalException {
        User user = userRepository.findByKeyCloakSub(this.getUserSub())
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        TaskDocument task = taskRepository.findById(taskId)
                                          .orElseThrow(() -> new TechnicalException(404, "Tâche non trouvée"));

        if (!task.isRoommate(user.getColocations()
                                 .stream()
                                 .map(Colocation::getId)
                                 .collect(Collectors.toSet())
        )) {
            throw new TechnicalException(403, "Vous n'avez pas accès à cette tâche");
        }

        task.setStatus(TaskDocument.TaskStatus.COMPLETED);
        TaskDocument updatedTask = taskRepository.save(task);
        return taskMapper.toTaskResDto(updatedTask);
    }

    /**
     * Deletes a task
     */
    public void deleteTask(String taskId) throws
                                          TechnicalException {
        User user = userRepository.findByKeyCloakSub(this.getUserSub())
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        TaskDocument task = taskRepository.findById(taskId)
                                          .orElseThrow(() -> new TechnicalException(404, "Tâche non trouvée"));

        if (!task.isRoommate(user.getColocations()
                                 .stream()
                                 .map(Colocation::getId)
                                 .collect(Collectors.toSet())
        )) {
            throw new TechnicalException(403, "Vous n'avez pas accès à cette tâche");
        }

        taskRepository.delete(task);
    }
}
