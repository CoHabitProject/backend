package fr.esgi.rest.interne;

import fr.esgi.domain.dto.user.UserRelationshipReqDto;
import fr.esgi.domain.dto.user.UserRelationshipResDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.service.space.UserRelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@RestController
@RequestMapping("/api/interne/user")
@RequiredArgsConstructor
@Tag(
        name = "User Relationships",
        description = "API endpoints for managing parent-child relationships between users"
)
@SecurityRequirement(name = "bearerAuth")
public class UserRest {

    private final UserRelationService userRelationService;

    /**
     * Endpoint to request a user relationship (parent-child).
     *
     * @param userRelationshipReqDto the request DTO containing relationship details
     * @return the response DTO containing relationship information
     * @throws TechnicalException if an error occurs during processing
     */
    @Operation(
            summary = "Request a user relationship",
            description = "Creates a parent-child relationship request between users. The requester must be authenticated and provide details of the target user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Relationship request created successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserRelationshipResDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid relationship type or malformed request",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User not authenticated",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found (requester or target)",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @PostMapping("/request-relation")
    public ResponseEntity<UserRelationshipResDto> requestRelation(
            @Parameter(
                    description = "Request details for creating a relationship",
                    required = true,
                    schema = @Schema(implementation = UserRelationshipReqDto.class)
            )
            @RequestBody UserRelationshipReqDto userRelationshipReqDto) throws TechnicalException {
        return ResponseEntity.ok(userRelationService.requestRelation(userRelationshipReqDto));
    }

    /**
     * Endpoint to get all relationships for the authenticated user.
     *
     * @return a list of user relationships
     * @throws TechnicalException if an error occurs during processing
     */
    @Operation(
            summary = "Get all user relationships",
            description = "Retrieves all parent-child relationships for the authenticated user, both as parent and as child",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Relationships retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = UserRelationshipResDto.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User not authenticated",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @GetMapping("/relations")
    public ResponseEntity<List<UserRelationshipResDto>> getAllRelations() throws TechnicalException {
        return ResponseEntity.ok(userRelationService.getAllRelationsForUser());
    }
}
