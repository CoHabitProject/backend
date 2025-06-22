package fr.esgi.persistence.repository.space;

import fr.esgi.persistence.entity.space.Colocation;
import fr.esgi.persistence.entity.user.User;
import fr.esgi.persistence.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ColocationRepositoryTest {

    @Autowired
    private ColocationRepository colocationRepository;

    @Autowired
    private UserRepository userRepository;

    private User manager;
    private User roommate1;
    private User roommate2;
    private Colocation colocation;

    @BeforeEach
    void setUp() {
        manager = new User();
        manager.setFirstName("Jean");
        manager.setLastName("Dupont");
        manager.setEmail("jean.dupont@test.com");
        manager.setUsername("jeandupont");
        manager.setKeyCloakSub("manager-keycloak-id");
        manager = userRepository.save(manager);

        roommate1 = new User();
        roommate1.setFirstName("Marie");
        roommate1.setLastName("Martin");
        roommate1.setEmail("marie.martin@test.com");
        roommate1.setUsername("mariemartin");
        roommate1.setKeyCloakSub("roommate1-keycloak-id");
        roommate1 = userRepository.save(roommate1);

        roommate2 = new User();
        roommate2.setFirstName("Pierre");
        roommate2.setLastName("Durand");
        roommate2.setEmail("pierre.durand@test.com");
        roommate2.setUsername("pierredurand");
        roommate2.setKeyCloakSub("roommate2-keycloak-id");
        roommate2 = userRepository.save(roommate2);

        // Création de la colocation
        colocation = new Colocation();
        colocation.setName("Appartement Centre-ville");
        colocation.setDescription("Bel appartement en centre-ville");
        colocation.setAddress("123 Rue de la Paix");
        colocation.setCity("Paris");
        colocation.setPostalCode("75001");
        colocation.setMaxRoommates(3);
        colocation.setManager(manager);
        colocation.addRoommate(roommate1);
        colocation.addRoommate(roommate2);
        colocation = colocationRepository.save(colocation);
    }

    @Test
    void testSaveAndFindColocation() {
        // Test de sauvegarde et récupération
        Optional<Colocation> found = colocationRepository.findById(colocation.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Appartement Centre-ville");
        assertThat(found.get().getManager()).isEqualTo(manager);
        assertThat(found.get().getRoommates()).hasSize(2);
        assertThat(found.get().getRoommates()).contains(roommate1, roommate2);
    }

    @Test
    void testFindByManager() {
        List<Colocation> managedColocations = colocationRepository.findByManager(manager);

        assertThat(managedColocations).hasSize(1);
        assertThat(managedColocations.get(0)).isEqualTo(colocation);
    }

    @Test
    void testFindByRoommate() {
        List<Colocation> roommateColocations = colocationRepository.findByRoommate(roommate1);

        assertThat(roommateColocations).hasSize(1);
        assertThat(roommateColocations.get(0)).isEqualTo(colocation);
    }

    @Test
    void testFindAllByUser() {
        // Le manager doit apparaître dans les résultats
        List<Colocation> managerColocations = colocationRepository.findAllByUser(manager);
        assertThat(managerColocations).hasSize(1);

        // Le colocataire aussi
        List<Colocation> roommateColocations = colocationRepository.findAllByUser(roommate1);
        assertThat(roommateColocations).hasSize(1);
    }

    @Test
    void testFindByCity() {
        List<Colocation> parisColocations = colocationRepository.findByCityIgnoreCase("paris");

        assertThat(parisColocations).hasSize(1);
        assertThat(parisColocations.get(0)).isEqualTo(colocation);
    }

    @Test
    void testFindAvailableColocations() {
        List<Colocation> availableColocations = colocationRepository.findAvailableColocations();

        // La colocation a 2 colocataires sur 3 max, donc elle est disponible
        assertThat(availableColocations).hasSize(1);
        assertThat(availableColocations).contains(colocation);
    }

    @Test
    void testIsUserInColocation() {
        // Test avec le manager
        boolean managerInColocation = colocationRepository.isUserInColocation(colocation.getId(), manager);
        assertThat(managerInColocation).isTrue();

        // Test avec un colocataire
        boolean roommateInColocation = colocationRepository.isUserInColocation(colocation.getId(), roommate1);
        assertThat(roommateInColocation).isTrue();

        // Test avec un utilisateur qui n'est pas dans la colocation
        User outsider = new User();
        outsider.setEmail("outsider@test.com");
        outsider.setUsername("outsider");
        outsider.setKeyCloakSub("outsider-keycloak-id");
        outsider = userRepository.save(outsider);

        boolean outsiderInColocation = colocationRepository.isUserInColocation(colocation.getId(), outsider);
        assertThat(outsiderInColocation).isFalse();
    }

    @Test
    void testCountRoommatesByColocationId() {
        Integer count = colocationRepository.countRoommatesByColocationId(colocation.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    void testColocationUtilityMethods() {
        assertThat(colocation.isManager(manager)).isTrue();
        assertThat(colocation.isManager(roommate1)).isFalse();

        assertThat(colocation.isRoommate(roommate1)).isTrue();
        assertThat(colocation.isRoommate(manager)).isFalse();

        assertThat(colocation.isFull()).isFalse();
        assertThat(colocation.getCurrentRoommatesCount()).isEqualTo(2);
    }

    @Test
    void testAddAndRemoveRoommate() {
        User newRoommate = new User();
        newRoommate.setEmail("new@test.com");
        newRoommate.setUsername("newroommate");
        newRoommate.setKeyCloakSub("new-keycloak-id");
        newRoommate = userRepository.save(newRoommate);

        // Ajout d'un colocataire
        colocation.addRoommate(newRoommate);
        colocation = colocationRepository.save(colocation);

        assertThat(colocation.getCurrentRoommatesCount()).isEqualTo(3);
        assertThat(colocation.isFull()).isTrue();

        // Suppression d'un colocataire
        colocation.removeRoommate(newRoommate);
        colocation = colocationRepository.save(colocation);

        assertThat(colocation.getCurrentRoommatesCount()).isEqualTo(2);
        assertThat(colocation.isFull()).isFalse();
    }
}
