package fr.esgi.domain.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRelationshipResDto {
    private Long    id;
    private Long    parentId;
    private String  parentEmail;
    private Long    childId;
    private String  childEmail;
    private boolean parentConfirmed;
    private boolean childConfirmed;
    private boolean fullyConfirmed;
}
