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
public class StockRepositoryTest {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ColocationRepository colocationRepository;

    @Autowired
    private UserRepository userRepository;

    private User        manager;
    private Colocation  colocation;
    private StockEntity fridgeStock;
    private StockEntity pantryStock;
    private StockEntity cleaningStock;

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
        colocation.setCity("Paris");
        colocation.setManager(manager);
        colocation = colocationRepository.save(colocation);

        // Création des stocks
        fridgeStock = new StockEntity();
        fridgeStock.setTitle("Frigo");
        fridgeStock.setColor("#00FF00");
        fridgeStock.setImageAsset("fridge.png");
        fridgeStock.setMaxCapacity(50);
        fridgeStock.setColocation(colocation);
        fridgeStock = stockRepository.save(fridgeStock);

        pantryStock = new StockEntity();
        pantryStock.setTitle("Garde-manger");
        pantryStock.setColor("#FFFF00");
        pantryStock.setImageAsset("pantry.png");
        pantryStock.setMaxCapacity(100);
        pantryStock.setColocation(colocation);
        pantryStock = stockRepository.save(pantryStock);

        cleaningStock = new StockEntity();
        cleaningStock.setTitle("Produits de nettoyage");
        cleaningStock.setColor("#FF0000");
        cleaningStock.setImageAsset("cleaning.png");
        cleaningStock.setColocation(colocation);
        cleaningStock = stockRepository.save(cleaningStock);
    }

    @Test
    void testSaveAndFindStock() {
        Optional<StockEntity> found = stockRepository.findById(fridgeStock.getId());

        assertThat(found).isPresent();
        assertThat(found.get()
                        .getTitle()).isEqualTo("Frigo");
        assertThat(found.get()
                        .getColor()).isEqualTo("#00FF00");
        assertThat(found.get()
                        .getMaxCapacity()).isEqualTo(50);
        assertThat(found.get()
                        .getColocation()).isEqualTo(colocation);
    }

    @Test
    void testFindByColocationId() {
        List<StockEntity> stocks = stockRepository.findByColocationId(colocation.getId());

        assertThat(stocks).hasSize(3);
        assertThat(stocks).contains(fridgeStock, pantryStock, cleaningStock);
    }

    @Test
    void testFindByIdAndColocationId() {
        Optional<StockEntity> found = stockRepository.findByIdAndColocationId(fridgeStock.getId(), colocation.getId());

        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(fridgeStock);

        // Test avec une colocation inexistante
        Optional<StockEntity> notFound = stockRepository.findByIdAndColocationId(fridgeStock.getId(), 999L);
        assertThat(notFound).isEmpty();
    }

    @Test
    void testFindByTitleContainingIgnoreCaseAndColocationId() {
        List<StockEntity> stocks = stockRepository.findByTitleContainingIgnoreCaseAndColocationId("frigo", colocation.getId());

        assertThat(stocks).hasSize(1);
        assertThat(stocks.get(0)).isEqualTo(fridgeStock);

        // Test insensible à la casse
        stocks = stockRepository.findByTitleContainingIgnoreCaseAndColocationId("GARDE", colocation.getId());
        assertThat(stocks).hasSize(1);
        assertThat(stocks.get(0)).isEqualTo(pantryStock);
    }

    @Test
    void testFindStocksWithCapacityByColocationId() {
        List<StockEntity> stocksWithCapacity = stockRepository.findStocksWithCapacityByColocationId(colocation.getId());

        assertThat(stocksWithCapacity).hasSize(2);
        assertThat(stocksWithCapacity).contains(fridgeStock, pantryStock);
        assertThat(stocksWithCapacity).doesNotContain(cleaningStock);
    }

    @Test
    void testFindByColocationIdAndColor() {
        List<StockEntity> greenStocks = stockRepository.findByColocationIdAndColor(colocation.getId(), "#00FF00");

        assertThat(greenStocks).hasSize(1);
        assertThat(greenStocks.get(0)).isEqualTo(fridgeStock);

        List<StockEntity> blueStocks = stockRepository.findByColocationIdAndColor(colocation.getId(), "#0000FF");
        assertThat(blueStocks).isEmpty();
    }

    @Test
    void testExistsByTitleAndColocationId() {
        boolean exists = stockRepository.existsByTitleAndColocationId("Frigo", colocation.getId());
        assertThat(exists).isTrue();

        boolean notExists = stockRepository.existsByTitleAndColocationId("Cave à vin", colocation.getId());
        assertThat(notExists).isFalse();
    }

    @Test
    void testStockUtilityMethods() {
        // Test des méthodes utilitaires
        assertThat(fridgeStock.getCurrentItemsCount()).isEqualTo(0);
        assertThat(fridgeStock.isAtCapacity()).isFalse();

        // Ajout d'items pour tester
        StockItemEntity item1 = new StockItemEntity("Lait", 2);
        StockItemEntity item2 = new StockItemEntity("Œufs", 12);

        fridgeStock.addItem(item1);
        fridgeStock.addItem(item2);

        assertThat(fridgeStock.getCurrentItemsCount()).isEqualTo(14);
        assertThat(fridgeStock.isAtCapacity()).isFalse();

        // Test avec stock sans capacité max
        assertThat(cleaningStock.isAtCapacity()).isFalse();
    }

    @Test
    void testAddAndRemoveItems() {
        StockItemEntity item = new StockItemEntity("Yaourt", 6);

        pantryStock.addItem(item);
        assertThat(pantryStock.getItems()).hasSize(1);
        assertThat(pantryStock.getItems()).contains(item);
        assertThat(item.getStock()).isEqualTo(pantryStock);

        pantryStock.removeItem(item);
        assertThat(pantryStock.getItems()).isEmpty();
        assertThat(item.getStock()).isNull();
    }

    @Test
    void testDeleteByIdAndColocationId() {
        Long stockId      = fridgeStock.getId();
        Long colocationId = colocation.getId();

        stockRepository.deleteByIdAndColocationId(stockId, colocationId);

        Optional<StockEntity> deleted = stockRepository.findById(stockId);
        assertThat(deleted).isEmpty();

        // Vérifier que les autres stocks existent encore
        List<StockEntity> remainingStocks = stockRepository.findByColocationId(colocationId);
        assertThat(remainingStocks).hasSize(2);
        assertThat(remainingStocks).contains(pantryStock, cleaningStock);
    }
}
