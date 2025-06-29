package fr.esgi.persistence.document;

import fr.esgi.persistence.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Document(indexName = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Keyword)
    private String userKeycloakSub;

    @Field(type = FieldType.Text, name = "user_name")
    private String userName;

    @Field(type = FieldType.Long)
    private Long colocationId;

    @Field(type = FieldType.Text, name = "colocation_name")
    private String colocationName;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private TaskStatus status;

    @Field(type = FieldType.Keyword)
    private TaskPriority priority;

    @Field(type = FieldType.Date, name = "created_at")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, name = "due_date")
    private LocalDateTime dueDate;

    @Field(type = FieldType.Date, name = "completed_at")
    private LocalDateTime completedAt;

    @Field(type = FieldType.Long, name = "creator_id")
    private Long creatorId;

    @Field(type = FieldType.Long)
    private Set<Long> assignedUserIds = new HashSet<>();

    @Field(type = FieldType.Keyword, name = "assigned_to_user_keycloak_subs")
    private Set<String> assignedToUserKeycloakSubs = new HashSet<>();

    @Field(type = FieldType.Keyword)
    private Set<String> tags = new HashSet<>();

    public enum TaskPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }

    public enum TaskStatus {
        TODO,
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    @Transient
    public boolean isRoommate(Set<Long> colocationsId) {
        return colocationsId.contains(this.getColocationId());
    }

    public void addAssignedUser(User user) {
        if (assignedToUserKeycloakSubs == null) {
            assignedToUserKeycloakSubs = new HashSet<>();
        }
        if (assignedUserIds == null) {
            assignedUserIds = new HashSet<>();
        }
        assignedToUserKeycloakSubs.add(user.getKeyCloakSub());
        assignedUserIds.add(user.getId());
    }
}
