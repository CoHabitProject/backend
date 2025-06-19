package fr.esgi.domain.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRelationshipReqDto {

    private String firstName;
    private String lastName;
    private String birthDate;
    private String whoAmI; // "parent" or "child"

}
