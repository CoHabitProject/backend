package fr.esgi.service.space;

import fr.esgi.domain.dto.user.UserRelationshipReqDto;
import fr.esgi.domain.dto.user.UserRelationshipResDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.domain.port.in.IUserRelationService;
import fr.esgi.domain.util.DateUtils;
import fr.esgi.persistence.entity.user.User;
import fr.esgi.persistence.entity.user.UserRelationship;
import fr.esgi.persistence.repository.user.UserRelationshipRepository;
import fr.esgi.persistence.repository.user.UserRepository;
import fr.esgi.service.AbstractService;
import fr.esgi.service.space.mapper.UserRelationshipMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRelationService extends AbstractService {

    private final UserRepository             userRepository;
    private final UserRelationshipRepository userRelationshipRepository;
    private final UserRelationshipMapper     userRelationshipMapper;


    public UserRelationshipResDto requestRelation(UserRelationshipReqDto dto) throws
                                                                              TechnicalException {

        User userRequester = userRepository.findByKeyCloakSub(this.getUserSub())
                                           .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        LocalDate birthDate = DateUtils.stringToLocalDate(dto.getBirthDate());

        User userRecipient = userRepository.findByFirstNameAndLastNameAndBirthDate(
                                                   dto.getFirstName(),
                                                   dto.getLastName(),
                                                   birthDate
                                           )
                                           .orElseThrow(() -> new TechnicalException(404, String.format(
                                                   "Votre %s n'est pas trouvé",
                                                   dto.getWhoAmI()
                                                      .equals(IUserRelationService.PARENT) ? "enfant" : "parent"
                                           )));

        UserRelationship userRelationship = UserRelationship.builder()
                                                            .parentConfirmed(false)
                                                            .childConfirmed(false)
                                                            .build();

        switch (dto.getWhoAmI()) {
            case IUserRelationService.PARENT:
                userRelationship.setChild(userRecipient);
                userRelationship.setParent(userRequester);
                userRelationship.setParentConfirmed(true);
                break;
            case IUserRelationService.CHILD:
                userRelationship.setChild(userRequester);
                userRelationship.setParent(userRecipient);
                userRelationship.setChildConfirmed(true);
                break;
            default:
                throw new TechnicalException(400, "Type de relation non supporté");
        }

        userRelationshipRepository.save(userRelationship);

        return userRelationshipMapper.toDto(userRelationship);
    }

    public List<UserRelationshipResDto> getAllRelationsForUser() throws TechnicalException {
        User authenticatedUser = userRepository.findByKeyCloakSub(this.getUserSub())
                                              .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        List<UserRelationship> relationships = userRelationshipRepository.findAllRelationshipsForUser(authenticatedUser.getId());
        
        return relationships.stream()
                           .map(userRelationshipMapper::toDto)
                           .toList();
    }

}
