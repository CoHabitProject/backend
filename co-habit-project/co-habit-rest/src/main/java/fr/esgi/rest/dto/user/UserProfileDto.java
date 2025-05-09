package fr.esgi.rest.dto.user;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserProfileDto {
    private String       id;
    private String       username;
    private String       email;
    private boolean      emailVerified;
    private List<String> roles;
}
