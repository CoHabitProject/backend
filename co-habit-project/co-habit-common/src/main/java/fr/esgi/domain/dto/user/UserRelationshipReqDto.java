package fr.esgi.domain.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request DTO for creating a user relationship")
public class UserRelationshipReqDto {

    @Schema(
            description = "First name of the target user",
            example = "John"
    )
    private String firstName;

    @Schema(
            description = "Last name of the target user",
            example = "Doe"
    )
    private String lastName;

    @Schema(
            description = "Birth date of the target user in format YYYY-MM-DD",
            example = "2000-01-01",
            pattern= "\\d{4}-\\d{2}-\\d{2}"
            )
    private String birthDate;

    @Schema(
            description = "Role of the requesting user in the relationship",
            example = "parent",
            allowableValues = {"parent", "child"}
    )
    private String whoAmI; // "parent" or "child"

}
