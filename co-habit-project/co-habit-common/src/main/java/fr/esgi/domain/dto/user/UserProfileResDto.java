package fr.esgi.domain.dto.user;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResDto {
    private Long   id;
    private String keyCloakSub;
    private String email;
    private String phoneNumber;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private String birthDate;
    private String gender;
    private String createdAt;
    private String updatedAt;
}
