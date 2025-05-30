package fr.esgi.persistence.repository.space;

import fr.esgi.persistence.entity.space.Colocation;
import fr.esgi.persistence.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColocationRepository extends JpaRepository<Colocation, Long> {

    // Trouver toutes les colocations gérées par un utilisateur
    List<Colocation> findByManager(User manager);

    // Trouver toutes les colocations où un utilisateur est colocataire
    @Query("SELECT c FROM Colocation c JOIN c.roommates r WHERE r = :user")
    List<Colocation> findByRoommate(@Param("user") User user);

    // Trouver toutes les colocations (gérées ou en tant que colocataire) d'un utilisateur
    @Query("SELECT DISTINCT c FROM Colocation c WHERE c.manager = :user OR :user MEMBER OF c.roommates")
    List<Colocation> findAllByUser(@Param("user") User user);

    // Trouver les colocations par ville
    List<Colocation> findByCityIgnoreCase(String city);

    // Trouver les colocations avec des places disponibles
    @Query("SELECT c FROM Colocation c WHERE c.maxRoommates IS NULL OR SIZE(c.roommates) < c.maxRoommates")
    List<Colocation> findAvailableColocations();

    // Trouver une colocation par nom (insensible à la casse)
    Optional<Colocation> findByNameIgnoreCase(String name);

    // Compter le nombre de colocataires dans une colocation
    @Query("SELECT SIZE(c.roommates) FROM Colocation c WHERE c.id = :colocationId")
    Integer countRoommatesByColocationId(@Param("colocationId") Long colocationId);

    // Vérifier si un utilisateur est dans une colocation spécifique
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Colocation c WHERE c.id = :colocationId AND (:user MEMBER OF c.roommates OR c.manager = :user)")
    boolean isUserInColocation(@Param("colocationId") Long colocationId, @Param("user") User user);
}
