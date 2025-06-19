package fr.esgi.rest.interne;

import fr.esgi.domain.dto.user.UserRelationshipReqDto;
import fr.esgi.domain.dto.user.UserRelationshipResDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.service.space.UserRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interne/user")
@RequiredArgsConstructor
public class UserRest {

    private final UserRelationService userRelationService;

    @PostMapping("/request-relation")
    UserRelationshipResDto requestRelation(@RequestBody
                                           UserRelationshipReqDto userRelationshipReqDto) throws
                                                                                          TechnicalException {
        return userRelationService.requestRelation(userRelationshipReqDto);
    }
}
