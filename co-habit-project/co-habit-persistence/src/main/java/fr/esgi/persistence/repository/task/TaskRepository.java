package fr.esgi.persistence.repository.task;

import fr.esgi.persistence.document.TaskDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends ElasticsearchRepository<TaskDocument, String> {
    
    List<TaskDocument> findByColocationId(Long colocationId);
    
    List<TaskDocument> findByUserKeycloakSub(String userKeycloakSub);
    
    List<TaskDocument> findByColocationIdAndStatus(Long colocationId, String status);
    
    List<TaskDocument> findByAssignedToUserKeycloakSubs(String assignedToUserKeycloakSub);
    
    List<TaskDocument> findByColocationIdAndAssignedToUserKeycloakSubs(Long colocationId, String userKeycloakSub);
}
