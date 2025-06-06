package fr.esgi.service.space;

import fr.esgi.domain.dto.user.UserRelationshipReqDto;
import fr.esgi.domain.dto.user.UserRelationshipResDto;
import fr.esgi.domain.exception.TechnicalException;
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

@Service
@RequiredArgsConstructor
public class UserRelationService extends AbstractService {

    private final UserRepository             userRepository;
    private final UserRelationshipRepository userRelationshipRepository;
    private final UserRelationshipMapper     userRelationshipMapper;


    public UserRelationshipResDto requestRelation(UserRelationshipReqDto userRelationshipReqDto) throws
                                                                                                 TechnicalException {

        User parent = userRepository.findByKeyCloakSub(getJwtAuthentication()
                                                               .getName())
                                    .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        LocalDate birthDate = DateUtils.stringToLocalDate(userRelationshipReqDto.getBirthDate());

        User child = userRepository.findByFirstNameAndLastNameAndBirthDate(
                                           userRelationshipReqDto.getFirstName(),
                                           userRelationshipReqDto.getLastName(),
                                           birthDate
                                   )
                                   .orElseThrow(() -> new TechnicalException(404, "Votre enfin n'est pas trouvé"));

        UserRelationship userRelationship = UserRelationship.builder()
                                                            .parent(parent)
                                                            .child(child)
                                                            .parentConfirmed(true)
                                                            .childConfirmed(false)
                                                            .build();

        userRelationshipRepository.save(userRelationship);

        return userRelationshipMapper.toDto(userRelationship);
    }

}
