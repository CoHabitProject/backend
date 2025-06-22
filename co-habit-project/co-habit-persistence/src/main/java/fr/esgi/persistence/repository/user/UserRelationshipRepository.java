package fr.esgi.persistence.repository.user;

import fr.esgi.persistence.entity.user.User;
import fr.esgi.persistence.entity.user.UserRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRelationshipRepository extends JpaRepository<UserRelationship, Long> {

    List<UserRelationship> findByParent(User parent);

    List<UserRelationship> findByChild(User child);

    Optional<UserRelationship> findByParentAndChild(User parent, User child);

    @Query(
            """
                    SELECT ur 
                    FROM UserRelationship ur 
                    WHERE 
                    ur.parentConfirmed = true 
                    AND ur.childConfirmed = true
                    """
    )
    List<UserRelationship> findAllFullyConfirmedRelationships();

    List<UserRelationship> findByParentConfirmed(boolean parentConfirmed);

    List<UserRelationship> findByChildConfirmed(boolean childConfirmed);

    @Query(
            """
                    SELECT ur 
                    FROM UserRelationship ur 
                    WHERE 
                    ur.parent.id = :userId 
                    OR ur.child.id = :userId
                    """
    )
    List<UserRelationship> findAllRelationshipsForUser(@Param("userId") Long userId);

    void deleteByParentAndChild(User parent, User child);
}
