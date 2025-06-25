package fr.esgi.service.space;

import fr.esgi.domain.dto.space.ColocationReqDto;
import fr.esgi.domain.dto.space.ColocationResDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.persistence.entity.space.Colocation;
import fr.esgi.persistence.entity.user.User;
import fr.esgi.persistence.repository.space.ColocationRepository;
import fr.esgi.persistence.repository.user.UserRepository;
import fr.esgi.service.AbstractService;
import fr.esgi.service.space.mapper.ColocationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ColocationService extends AbstractService {

    private final ColocationRepository colocationRepository;
    private final UserRepository       userRepository;
    private final ColocationMapper     colocationMapper;

    /**
     * Creates a new colocation with the authenticated user as manager
     */
    public ColocationResDto createColocation(ColocationReqDto dto) throws
                                                                   TechnicalException {
        String userSub = getUserSub();

        User manager = userRepository.findByKeyCloakSub(userSub)
                                     .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        Colocation colocation = colocationMapper.mapDtoToColocation(dto);
        colocation.setManager(manager);
        colocation.setInvitationCode(generateInvitationCode());

        // Add manager as first roommate
        colocation.addRoommate(manager);

        Colocation savedColocation = colocationRepository.save(colocation);
        return colocationMapper.mapColocationToResDto(savedColocation);
    }

    /**
     * Updates an existing colocation (only manager can update)
     */
    public ColocationResDto updateColocation(Long colocationId, ColocationReqDto dto) throws
                                                                                      TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        Colocation colocation = colocationRepository.findById(colocationId)
                                                    .orElseThrow(() -> new TechnicalException(404, "Colocation non trouvée"));

        if (!colocation.isManager(user)) {
            throw new TechnicalException(403, "Seul le gestionnaire peut modifier cette colocation");
        }

        colocationMapper.updateColocationFromDto(dto, colocation);
        Colocation updatedColocation = colocationRepository.save(colocation);
        return colocationMapper.mapColocationToResDto(updatedColocation);
    }

    /**
     * Gets a colocation by ID (only accessible to members)
     */
    @Transactional(readOnly = true)
    public ColocationResDto getColocationById(Long colocationId) throws
                                                                 TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        Colocation colocation = colocationRepository.findById(colocationId)
                                                    .orElseThrow(() -> new TechnicalException(404, "Colocation non trouvée"));

        if (!colocation.isRoommate(user)) {
            throw new TechnicalException(403, "Vous n'avez pas accès à cette colocation");
        }

        return colocationMapper.mapColocationToResDto(colocation);
    }

    /**
     * Gets all colocations managed by the authenticated user
     */
    @Transactional(readOnly = true)
    public List<ColocationResDto> getManagedColocations() throws
                                                          TechnicalException {
        String userSub = getUserSub();

        User manager = userRepository.findByKeyCloakSub(userSub)
                                     .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        List<Colocation> colocations = colocationRepository.findByManager(manager);
        return colocationMapper.mapColocationsToResDtos(colocations);
    }

    /**
     * Gets all colocations where the user is a roommate
     */
    @Transactional(readOnly = true)
    public List<ColocationResDto> getUserColocations() throws
                                                       TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        List<Colocation> colocations = colocationRepository.findByRoommatesContaining(user);
        return colocationMapper.mapColocationsToResDtos(colocations);
    }

    /**
     * Deletes a colocation (only manager can delete)
     */
    public void deleteColocation(Long colocationId) throws
                                                    TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        Colocation colocation = colocationRepository.findById(colocationId)
                                                    .orElseThrow(() -> new TechnicalException(404, "Colocation non trouvée"));

        if (!colocation.isManager(user)) {
            throw new TechnicalException(403, "Seul le gestionnaire peut supprimer cette colocation");
        }

        colocationRepository.delete(colocation);
    }

    /**
     * Joins a colocation using invitation code
     */
    public ColocationResDto joinColocation(String invitationCode) throws
                                                                  TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        Colocation colocation = colocationRepository.findByInvitationCode(invitationCode)
                                                    .orElseThrow(() -> new TechnicalException(404, "Code d'invitation invalide"));

        if (colocation.isRoommate(user)) {
            throw new TechnicalException(409, "Vous êtes déjà membre de cette colocation");
        }

        if (colocation.isFull()) {
            throw new TechnicalException(409, "Cette colocation est pleine");
        }

        colocation.addRoommate(user);
        Colocation updatedColocation = colocationRepository.save(colocation);
        return colocationMapper.mapColocationToResDto(updatedColocation);
    }

    /**
     * Leaves a colocation
     */
    public void leaveColocation(Long colocationId) throws
                                                   TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        Colocation colocation = colocationRepository.findById(colocationId)
                                                    .orElseThrow(() -> new TechnicalException(404, "Colocation non trouvée"));

        if (!colocation.isRoommate(user)) {
            throw new TechnicalException(409, "Vous n'êtes pas membre de cette colocation");
        }

        if (colocation.isManager(user)) {
            throw new TechnicalException(409, "Le gestionnaire ne peut pas quitter la colocation. Supprimez-la ou transférez la gestion.");
        }

        colocation.removeRoommate(user);
        colocationRepository.save(colocation);
    }

    private String generateInvitationCode() {
        return UUID.randomUUID()
                   .toString()
                   .substring(0, 8)
                   .toUpperCase();
    }
}
