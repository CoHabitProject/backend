package fr.esgi.persistence.repository;

import fr.esgi.persistence.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByKeyCloakSub(String keyCloakSub);
    
    List<User> findByFirstNameContainingIgnoreCase(String firstName);
    
    List<User> findByLastNameContainingIgnoreCase(String lastName);
    
    List<User> findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
            String firstName, String lastName);
    
    @Query("SELECT DISTINCT u FROM User u JOIN u.children c WHERE c.parentConfirmed = true")
    List<User> findAllParentsWithConfirmedRelationships();
    
    @Query("SELECT DISTINCT u FROM User u JOIN u.parents p WHERE p.childConfirmed = true")
    List<User> findAllChildrenWithConfirmedRelationships();
    
    boolean existsByEmail(String email);
}
