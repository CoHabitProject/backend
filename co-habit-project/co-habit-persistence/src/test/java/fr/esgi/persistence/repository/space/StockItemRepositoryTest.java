package fr.esgi.persistence.repository.space;

import fr.esgi.persistence.entity.space.Colocation;
import fr.esgi.persistence.entity.space.StockEntity;
import fr.esgi.persistence.entity.space.StockItemEntity;
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
public class StockItemRepositoryTest {

    @Autowired
    private StockItemRepository stockItemRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ColocationRepository colocationRepository;

    @Autowired
    private UserRepository userRepository;

    private User            manager;
    private Colocation      colocation;
    private StockEntity     fridgeStock;
    private StockEntity     pantryStock;
    private StockItemEntity milk;
    private StockItemEntity eggs;
    private StockItemEntity emptyItem;
    private StockItemEntity rice;

    @BeforeEach
    void setUp() {
        manager = new User();
        manager.setFirstName("Jean");
        manager.setLastName("Dupont");
        manager.setEmail("jean.dupont@test.com");
        manager.setUsername("jeandupont");
        manager.setKeyCloakSub("manager-keycloak-id");
        manager = userRepository.save(manager);

        colocation = new Colocation();
        colocation.setName("Appartement Centre-ville");
        colocation.setAddress("123 Rue de la Paix");
        colocation.setManager(manager);
        colocation = colocationRepository.save(colocation);

        fridgeStock = new StockEntity();
        fridgeStock.setTitle("Frigo");
        fridgeStock.setColocation(colocation);
        fridgeStock = stockRepository.save(fridgeStock);

        pantryStock = new StockEntity();
        pantryStock.setTitle("Garde-manger");
        pantryStock.setColocation(colocation);
        pantryStock = stockRepository.save(pantryStock);

        // Création des items avec addedBy
        milk = new StockItemEntity("Lait", 2, manager);
        milk.setStock(fridgeStock);
        milk = stockItemRepository.save(milk);

        eggs = new StockItemEntity("Œufs", 12, manager);
        eggs.setStock(fridgeStock);
        eggs = stockItemRepository.save(eggs);

        emptyItem = new StockItemEntity("Beurre", 0, manager);
        emptyItem.setStock(fridgeStock);
        emptyItem = stockItemRepository.save(emptyItem);

        rice = new StockItemEntity("Riz", 5, manager);
        rice.setStock(pantryStock);
        rice = stockItemRepository.save(rice);
    }

    @Test
    void testSaveAndFindStockItem() {
        Optional<StockItemEntity> found = stockItemRepository.findById(milk.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Lait");
        assertThat(found.get().getQuantity()).isEqualTo(2);
        assertThat(found.get().getStock()).isEqualTo(fridgeStock);
        assertThat(found.get().getAddedBy()).isEqualTo(manager);
        assertThat(found.get().getAddedAt()).isNotNull();
    }

    @Test
    void testFindByStockId() {
        List<StockItemEntity> fridgeItems = stockItemRepository.findByStockId(fridgeStock.getId());

        assertThat(fridgeItems).hasSize(3);
        assertThat(fridgeItems).contains(milk, eggs, emptyItem);

        List<StockItemEntity> pantryItems = stockItemRepository.findByStockId(pantryStock.getId());
        assertThat(pantryItems).hasSize(1);
        assertThat(pantryItems).contains(rice);
    }

    @Test
    void testFindByIdAndStockId() {
        Optional<StockItemEntity> found = stockItemRepository.findByIdAndStockId(milk.getId(), fridgeStock.getId());

        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(milk);

        // Test avec un stock différent
        Optional<StockItemEntity> notFound = stockItemRepository.findByIdAndStockId(milk.getId(), pantryStock.getId());
        assertThat(notFound).isEmpty();
    }

    @Test
    void testFindByNameContainingIgnoreCaseAndStockId() {
        List<StockItemEntity> items = stockItemRepository.findByNameContainingIgnoreCaseAndStockId("lai", fridgeStock.getId());

        assertThat(items).hasSize(1);
        assertThat(items.get(0)).isEqualTo(milk);

        // Test insensible à la casse
        items = stockItemRepository.findByNameContainingIgnoreCaseAndStockId("ŒUFS", fridgeStock.getId());
        assertThat(items).hasSize(1);
        assertThat(items.get(0)).isEqualTo(eggs);
    }

    @Test
    void testFindAvailableItemsByStockId() {
        List<StockItemEntity> availableItems = stockItemRepository.findAvailableItemsByStockId(fridgeStock.getId());

        assertThat(availableItems).hasSize(2);
        assertThat(availableItems).contains(milk, eggs);
        assertThat(availableItems).doesNotContain(emptyItem);
    }

    @Test
    void testFindOutOfStockItemsByStockId() {
        List<StockItemEntity> outOfStockItems = stockItemRepository.findOutOfStockItemsByStockId(fridgeStock.getId());

        assertThat(outOfStockItems).hasSize(1);
        assertThat(outOfStockItems).contains(emptyItem);
        assertThat(outOfStockItems).doesNotContain(milk, eggs);
    }

    @Test
    void testFindByColocationId() {
        List<StockItemEntity> allItems = stockItemRepository.findByColocationId(colocation.getId());

        assertThat(allItems).hasSize(4);
        assertThat(allItems).contains(milk, eggs, emptyItem, rice);
    }

    @Test
    void testFindByNameContainingAndColocationId() {
        List<StockItemEntity> items = stockItemRepository.findByNameContainingAndColocationId("i", colocation.getId());

        // Items contenant "i": Lait, Riz
        assertThat(items).hasSize(2);
        assertThat(items).contains(milk, rice);
    }

    @Test
    void testGetTotalQuantityByStockId() {
        Integer totalQuantity = stockItemRepository.getTotalQuantityByStockId(fridgeStock.getId());

        // Lait: 2, Œufs: 12, Beurre: 0 = 14 total
        assertThat(totalQuantity).isEqualTo(14);

        Integer pantryQuantity = stockItemRepository.getTotalQuantityByStockId(pantryStock.getId());
        assertThat(pantryQuantity).isEqualTo(5);
    }

    @Test
    void testExistsByNameAndStockId() {
        boolean exists = stockItemRepository.existsByNameAndStockId("Lait", fridgeStock.getId());
        assertThat(exists).isTrue();

        boolean notExists = stockItemRepository.existsByNameAndStockId("Fromage", fridgeStock.getId());
        assertThat(notExists).isFalse();

        // Test dans un autre stock
        boolean existsInPantry = stockItemRepository.existsByNameAndStockId("Lait", pantryStock.getId());
        assertThat(existsInPantry).isFalse();
    }

    @Test
    void testStockItemUtilityMethods() {
        // Test increase quantity
        int initialQuantity = milk.getQuantity();
        milk.increaseQuantity(3);
        assertThat(milk.getQuantity()).isEqualTo(initialQuantity + 3);

        // Test decrease quantity
        milk.decreaseQuantity(2);
        assertThat(milk.getQuantity()).isEqualTo(initialQuantity + 1);

        // Test decrease below zero
        milk.decreaseQuantity(10);
        assertThat(milk.getQuantity()).isEqualTo(0);
    }

    @Test
    void testDeleteByIdAndStockId() {
        Long itemId  = milk.getId();
        Long stockId = fridgeStock.getId();

        stockItemRepository.deleteByIdAndStockId(itemId, stockId);

        Optional<StockItemEntity> deleted = stockItemRepository.findById(itemId);
        assertThat(deleted).isEmpty();

        // Vérifier que les autres items existent encore
        List<StockItemEntity> remainingItems = stockItemRepository.findByStockId(stockId);
        assertThat(remainingItems).hasSize(2);
        assertThat(remainingItems).contains(eggs, emptyItem);
    }

    @Test
    void testCreateItemWithConstructor() {
        StockItemEntity newItem = new StockItemEntity("Yaourt", 6);

        assertThat(newItem.getName()).isEqualTo("Yaourt");
        assertThat(newItem.getQuantity()).isEqualTo(6);
        assertThat(newItem.getStock()).isNull();
    }

    @Test
    void testFindByAddedBy() {
        List<StockItemEntity> itemsByManager = stockItemRepository.findByAddedBy(manager);

        assertThat(itemsByManager).hasSize(4);
        assertThat(itemsByManager).contains(milk, eggs, emptyItem, rice);
    }

    @Test
    void testFindByStockIdAndAddedBy() {
        List<StockItemEntity> fridgeItemsByManager = stockItemRepository.findByStockIdAndAddedBy(fridgeStock.getId(), manager);

        assertThat(fridgeItemsByManager).hasSize(3);
        assertThat(fridgeItemsByManager).contains(milk, eggs, emptyItem);
    }

    @Test
    void testFindByColocationIdAndAddedBy() {
        List<StockItemEntity> itemsByManagerInColocation = stockItemRepository.findByColocationIdAndAddedBy(colocation.getId(), manager);

        assertThat(itemsByManagerInColocation).hasSize(4);
        assertThat(itemsByManagerInColocation).contains(milk, eggs, emptyItem, rice);
    }

    @Test
    void testCountByAddedBy() {
        Long count = stockItemRepository.countByAddedBy(manager);

        assertThat(count).isEqualTo(4);
    }

    @Test
    void testCreateItemWithConstructorAndAddedBy() {
        StockItemEntity newItem = new StockItemEntity("Yaourt", 6, manager);
        
        assertThat(newItem.getName()).isEqualTo("Yaourt");
        assertThat(newItem.getQuantity()).isEqualTo(6);
        assertThat(newItem.getAddedBy()).isEqualTo(manager);
        assertThat(newItem.getStock()).isNull();
    }
}
