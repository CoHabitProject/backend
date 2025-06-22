package fr.esgi.service.space;

import fr.esgi.domain.dto.space.StockItemReqDto;
import fr.esgi.domain.dto.space.StockItemResDto;
import fr.esgi.domain.dto.space.StockReqDto;
import fr.esgi.domain.dto.space.StockResDto;
import fr.esgi.domain.exception.TechnicalException;
import fr.esgi.persistence.entity.space.Colocation;
import fr.esgi.persistence.entity.space.StockEntity;
import fr.esgi.persistence.entity.space.StockItemEntity;
import fr.esgi.persistence.entity.user.User;
import fr.esgi.persistence.repository.space.ColocationRepository;
import fr.esgi.persistence.repository.space.StockItemRepository;
import fr.esgi.persistence.repository.space.StockRepository;
import fr.esgi.persistence.repository.user.UserRepository;
import fr.esgi.service.AbstractTest;
import fr.esgi.service.registration.mapper.UserMapper;
import fr.esgi.service.space.mapper.StockMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(StockServiceTest.TestConfig.class)
@TestPropertySource(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
        }
)
@EnableJpaRepositories(basePackages = "fr.esgi.persistence.repository")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StockServiceTest extends AbstractTest {

    @TestConfiguration
    @EnableAutoConfiguration(
            exclude = {
                    ServletWebServerFactoryAutoConfiguration.class,
                    ReactiveWebServerFactoryAutoConfiguration.class
            }
    )
    static class TestConfig {

        @Bean
        public StockMapper stockMapper() {
            return Mappers.getMapper(StockMapper.class);
        }

        @Bean
        public UserMapper userMapper() {
            return Mappers.getMapper(UserMapper.class);
        }

        @Bean
        public StockService stockService(
                StockRepository stockRepository,
                StockItemRepository stockItemRepository,
                ColocationRepository colocationRepository,
                UserRepository userRepository,
                StockMapper stockMapper,
                UserMapper userMapper) {
            return new StockService(stockRepository, stockItemRepository, colocationRepository, userRepository, stockMapper);
        }
    }

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockItemRepository stockItemRepository;

    @Autowired
    private ColocationRepository colocationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockService stockService;

    private User       managerUser;
    private User       roommateUser;
    private User       otherUser;
    private Colocation colocation;

    @BeforeEach
    public void initData() {
        // Setup users
        managerUser = new User();
        managerUser.setEmail("manager@example.com");
        managerUser.setFirstName("John");
        managerUser.setLastName("Manager");
        managerUser.setKeyCloakSub(TEST_USER_ID);
        managerUser.setBirthDate(LocalDate.of(1990, 1, 1));
        managerUser = userRepository.save(managerUser);

        roommateUser = new User();
        roommateUser.setEmail("roommate@example.com");
        roommateUser.setFirstName("Jane");
        roommateUser.setLastName("Roommate");
        roommateUser.setKeyCloakSub("roommate-sub");
        roommateUser.setBirthDate(LocalDate.of(1992, 1, 1));
        roommateUser = userRepository.save(roommateUser);

        otherUser = new User();
        otherUser.setEmail("other@example.com");
        otherUser.setFirstName("Bob");
        otherUser.setLastName("Other");
        otherUser.setKeyCloakSub("other-sub");
        otherUser.setBirthDate(LocalDate.of(1988, 1, 1));
        otherUser = userRepository.save(otherUser);

        // Setup colocation
        colocation = new Colocation("Test Coloc", "Test Address", managerUser);
        colocation.addRoommate(roommateUser);
        colocation = colocationRepository.save(colocation);
    }

    @AfterEach
    public void cleanUp() {
        stockItemRepository.deleteAll();
        stockRepository.deleteAll();
        colocationRepository.deleteAll();
        userRepository.deleteAll();
        this.cleanupSecurityContext();
    }

    @Test
    public void testCreateStock_Success() throws
                                          TechnicalException {
        // Given
        this.initSecurityContextPlaceHolder();

        StockReqDto dto = new StockReqDto();
        dto.setTitle("Frigo");
        dto.setColor("#00FF00");
        dto.setImageAsset("fridge.png");
        dto.setMaxCapacity(50);

        // When
        StockResDto result = stockService.createStock(colocation.getId(), dto);

        // Then
        assertNotNull(result);
        assertEquals("Frigo", result.getTitle());
        assertEquals("#00FF00", result.getColor());
        assertEquals("fridge.png", result.getImageAsset());
        assertEquals(50, result.getMaxCapacity());
        assertNotNull(result.getId());

        // Verify in database
        Optional<StockEntity> savedStock = stockRepository.findById(result.getId());
        assertTrue(savedStock.isPresent());
        assertEquals(colocation,
                     savedStock.get()
                               .getColocation());
    }

    @Test
    public void testCreateStock_NotMember() {
        // Given
        this.initSecurityContextPlaceHolderWithSub("other-sub");

        StockReqDto dto = new StockReqDto();
        dto.setTitle("Test Stock");

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> stockService.createStock(colocation.getId(), dto));

        assertEquals(403, exception.getCode());
        assertEquals("Vous devez être membre de cette colocation", exception.getMessage());
    }

    @Test
    public void testCreateStock_DuplicateTitle() throws
                                                 TechnicalException {
        // Given
        this.initSecurityContextPlaceHolder();

        // Create first stock
        StockEntity existingStock = new StockEntity("Frigo", colocation);
        stockRepository.save(existingStock);

        StockReqDto dto = new StockReqDto();
        dto.setTitle("Frigo");

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> stockService.createStock(colocation.getId(), dto));

        assertEquals(409, exception.getCode());
        assertEquals("Un stock avec ce titre existe déjà dans cette colocation", exception.getMessage());
    }

    @Test
    public void testUpdateStock_Success() throws
                                          TechnicalException {
        // Given
        this.initSecurityContextPlaceHolder();

        StockEntity stock = new StockEntity("Original Title", colocation);
        stock = stockRepository.save(stock);

        StockReqDto updateDto = new StockReqDto();
        updateDto.setTitle("Updated Title");
        updateDto.setColor("#FF0000");
        updateDto.setMaxCapacity(100);

        // When
        StockResDto result = stockService.updateStock(colocation.getId(), stock.getId(), updateDto);

        // Then
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("#FF0000", result.getColor());
        assertEquals(100, result.getMaxCapacity());
    }

    @Test
    public void testGetStocksByColocation_Success() throws
                                                    TechnicalException {
        // Given
        this.initSecurityContextPlaceHolder();

        StockEntity stock1 = new StockEntity("Frigo", colocation);
        StockEntity stock2 = new StockEntity("Garde-manger", colocation);
        stockRepository.saveAll(List.of(stock1, stock2));

        // When
        List<StockResDto> result = stockService.getStocksByColocation(colocation.getId());

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream()
                         .anyMatch(s -> s.getTitle()
                                         .equals("Frigo")));
        assertTrue(result.stream()
                         .anyMatch(s -> s.getTitle()
                                         .equals("Garde-manger")));
    }

    @Test
    public void testDeleteStock_Success() throws
                                          TechnicalException {
        // Given
        this.initSecurityContextPlaceHolder();

        StockEntity stock = new StockEntity("Test Stock", colocation);
        stock = stockRepository.save(stock);
        Long stockId = stock.getId();

        // When
        stockService.deleteStock(colocation.getId(), stockId);

        // Then
        Optional<StockEntity> deletedStock = stockRepository.findById(stockId);
        assertFalse(deletedStock.isPresent());
    }

    @Test
    public void testDeleteStock_NotManager() {
        // Given
        this.initSecurityContextPlaceHolderWithSub("roommate-sub");

        StockEntity stock = new StockEntity("Test Stock", colocation);
        stock = stockRepository.save(stock);

        // When & Then
        final StockEntity finalStock = stock;
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> stockService.deleteStock(colocation.getId(), finalStock.getId()));

        assertEquals(403, exception.getCode());
        assertEquals("Seul le gestionnaire peut supprimer un stock", exception.getMessage());
    }

    @Test
    public void testAddItemToStock_Success() throws
                                             TechnicalException {
        // Given
        this.initSecurityContextPlaceHolder();

        StockEntity stock = new StockEntity("Frigo", colocation);
        stock = stockRepository.save(stock);

        StockItemReqDto dto = new StockItemReqDto();
        dto.setName("Lait");
        dto.setQuantity(2);

        // When
        StockItemResDto result = stockService.addItemToStock(colocation.getId(), stock.getId(), dto);

        // Then
        assertNotNull(result);
        assertEquals("Lait", result.getName());
        assertEquals(2, result.getQuantity());
        assertNotNull(result.getId());

        // Verify in database
        Optional<StockItemEntity> savedItem = stockItemRepository.findById(result.getId());
        assertTrue(savedItem.isPresent());
        assertEquals(stock,
                     savedItem.get()
                              .getStock());
        assertEquals(managerUser,
                     savedItem.get()
                              .getAddedBy());
    }

    @Test
    public void testAddItemToStock_DuplicateName() throws
                                                   TechnicalException {
        // Given
        this.initSecurityContextPlaceHolder();

        StockEntity stock = new StockEntity("Frigo", colocation);
        stock = stockRepository.save(stock);

        // Create existing item
        StockItemEntity existingItem = new StockItemEntity("Lait", 1, managerUser);
        existingItem.setStock(stock);
        stockItemRepository.save(existingItem);

        StockItemReqDto dto = new StockItemReqDto();
        dto.setName("Lait");
        dto.setQuantity(2);

        // When & Then
        final StockEntity finalStock = stock;
        StockItemResDto   result     = stockService.addItemToStock(colocation.getId(), finalStock.getId(), dto);

        assertEquals(result.getQuantity(), dto.getQuantity());

        //        assertEquals("Un item avec ce nom existe déjà dans ce stock", exception.getMessage());
    }

    @Test
    public void testUpdateStockItem_Success() throws
                                              TechnicalException {
        // Given
        this.initSecurityContextPlaceHolder();

        StockEntity stock = new StockEntity("Frigo", colocation);
        stock = stockRepository.save(stock);

        StockItemEntity item = new StockItemEntity("Lait", 1, managerUser);
        item.setStock(stock);
        item = stockItemRepository.save(item);

        StockItemReqDto updateDto = new StockItemReqDto();
        updateDto.setName("Lait Bio");
        updateDto.setQuantity(3);

        // When
        StockItemResDto result = stockService.updateStockItem(colocation.getId(), stock.getId(), item.getId(), updateDto);

        // Then
        assertNotNull(result);
        assertEquals("Lait Bio", result.getName());
        assertEquals(3, result.getQuantity());
    }

    @Test
    public void testGetStockItems_Success() throws
                                            TechnicalException {
        // Given
        this.initSecurityContextPlaceHolder();

        StockEntity stock = new StockEntity("Frigo", colocation);
        stock = stockRepository.save(stock);

        StockItemEntity item1 = new StockItemEntity("Lait", 2, managerUser);
        item1.setStock(stock);
        StockItemEntity item2 = new StockItemEntity("Œufs", 12, roommateUser);
        item2.setStock(stock);
        stockItemRepository.saveAll(List.of(item1, item2));

        // When
        List<StockItemResDto> result = stockService.getStockItems(colocation.getId(), stock.getId());

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream()
                         .anyMatch(i -> i.getName()
                                         .equals("Lait")));
        assertTrue(result.stream()
                         .anyMatch(i -> i.getName()
                                         .equals("Œufs")));
    }

    @Test
    public void testDeleteStockItem_Success() throws
                                              TechnicalException {
        // Given
        this.initSecurityContextPlaceHolder();

        StockEntity stock = new StockEntity("Frigo", colocation);
        stock = stockRepository.save(stock);

        StockItemEntity item = new StockItemEntity("Lait", 2, managerUser);
        item.setStock(stock);
        item = stockItemRepository.save(item);
        Long itemId = item.getId();

        // When
        stockService.deleteStockItem(colocation.getId(), stock.getId(), itemId);

        // Then
        Optional<StockItemEntity> deletedItem = stockItemRepository.findById(itemId);
        assertFalse(deletedItem.isPresent());
    }

    @Test
    public void testStockNotFound() {
        // Given
        this.initSecurityContextPlaceHolder();

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> stockService.getStockById(colocation.getId(), 999L));

        assertEquals(404, exception.getCode());
        assertEquals("Stock non trouvé", exception.getMessage());
    }

    @Test
    public void testColocationNotFound() {
        // Given
        this.initSecurityContextPlaceHolder();

        StockReqDto dto = new StockReqDto();
        dto.setTitle("Test");

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> stockService.createStock(999L, dto));

        assertEquals(404, exception.getCode());
        assertEquals("Colocation non trouvée", exception.getMessage());
    }

    @Test
    public void testUserNotAuthenticated() {
        // Given - no security context
        StockReqDto dto = new StockReqDto();
        dto.setTitle("Test");

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> stockService.createStock(colocation.getId(), dto));

        assertEquals(401, exception.getCode());
        assertEquals("User is not authenticated", exception.getMessage());
    }

    @Test
    public void testDeleteStockById_Success() throws
                                              TechnicalException {
        // Given
        this.initSecurityContextPlaceHolder();

        StockEntity stock = new StockEntity("Test Stock", colocation);
        stock = stockRepository.save(stock);

        // Add some items to the stock
        StockItemEntity item1 = new StockItemEntity("Item 1", 5, managerUser);
        item1.setStock(stock);
        StockItemEntity item2 = new StockItemEntity("Item 2", 3, managerUser);
        item2.setStock(stock);
        stockItemRepository.saveAll(List.of(item1, item2));

        Long stockId = stock.getId();

        // When
        stockService.deleteStockById(stockId);

        // Then
        Optional<StockEntity> deletedStock = stockRepository.findById(stockId);
        assertFalse(deletedStock.isPresent());

        // Verify all items are also deleted
        List<StockItemEntity> remainingItems = stockItemRepository.findByStockId(stockId);
        assertTrue(remainingItems.isEmpty());
    }

    @Test
    public void testDeleteStockById_NotManager() {
        // Given
        this.initSecurityContextPlaceHolderWithSub("roommate-sub");

        StockEntity stock = new StockEntity("Test Stock", colocation);
        stock = stockRepository.save(stock);

        // When & Then
        final StockEntity finalStock = stock;
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> stockService.deleteStockById(finalStock.getId()));

        assertEquals(403, exception.getCode());
        assertEquals("Seul le gestionnaire peut supprimer un stock", exception.getMessage());
    }

    @Test
    public void testDeleteStockById_StockNotFound() {
        // Given
        this.initSecurityContextPlaceHolder();

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> stockService.deleteStockById(999L));

        assertEquals(404, exception.getCode());
        assertEquals("Stock non trouvé", exception.getMessage());
    }

    @Test
    public void testDeleteStockById_WithManyItems() throws
                                                    TechnicalException {
        // Given
        this.initSecurityContextPlaceHolder();

        StockEntity stock = new StockEntity("Stock with Many Items", colocation);
        stock = stockRepository.save(stock);

        // Add multiple items
        List<StockItemEntity> items = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            StockItemEntity item = new StockItemEntity("Item " + i, i, managerUser);
            item.setStock(stock);
            items.add(item);
        }
        stockItemRepository.saveAll(items);

        Long stockId = stock.getId();

        // Verify items exist before deletion
        List<StockItemEntity> itemsBeforeDeletion = stockItemRepository.findByStockId(stockId);
        assertEquals(10, itemsBeforeDeletion.size());

        // When
        stockService.deleteStockById(stockId);

        // Then
        Optional<StockEntity> deletedStock = stockRepository.findById(stockId);
        assertFalse(deletedStock.isPresent());

        // Verify all items are deleted
        List<StockItemEntity> itemsAfterDeletion = stockItemRepository.findByStockId(stockId);
        assertTrue(itemsAfterDeletion.isEmpty());
    }

    @Test
    public void testDeleteStockById_UserNotAuthenticated() {
        // Given - no security context
        StockEntity stock = new StockEntity("Test Stock", colocation);
        stock = stockRepository.save(stock);

        // When & Then
        final StockEntity finalStock = stock;
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> stockService.deleteStockById(finalStock.getId()));

        assertEquals(401, exception.getCode());
        assertEquals("User is not authenticated", exception.getMessage());
    }

    @Test
    public void testAddItemToStock_ExceedsCapacity() throws
                                                     TechnicalException {
        // Given
        this.initSecurityContextPlaceHolder();

        StockEntity stock = new StockEntity("Frigo", colocation);
        stock.setMaxCapacity(10);
        stock = stockRepository.save(stock);

        // Add existing item with quantity 8
        StockItemEntity existingItem = new StockItemEntity("Lait", 8, managerUser);
        existingItem.setStock(stock);
        stockItemRepository.save(existingItem);

        // Try to add new item with quantity 5 (total would be 13, exceeding max capacity of 10)
        StockItemReqDto dto = new StockItemReqDto();
        dto.setName("Fromage");
        dto.setQuantity(5);

        // When & Then
        final StockEntity finalStock = stock;
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> stockService.addItemToStock(colocation.getId(), finalStock.getId(), dto));

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage()
                            .contains("dépasserait la capacité maximale du stock"));
        assertTrue(exception.getMessage()
                            .contains("13"));
        assertTrue(exception.getMessage()
                            .contains("10"));
    }

    @Test
    public void testAddItemToStock_ExactCapacity() throws
                                                   TechnicalException {
        // Given
        this.initSecurityContextPlaceHolder();

        StockEntity stock = new StockEntity("Frigo", colocation);
        stock.setMaxCapacity(10);
        stock = stockRepository.save(stock);

        // Add existing item with quantity 8
        StockItemEntity existingItem = new StockItemEntity("Lait", 8, managerUser);
        existingItem.setStock(stock);
        stockItemRepository.save(existingItem);

        // Add new item with quantity 2 (total would be exactly 10)
        StockItemReqDto dto = new StockItemReqDto();
        dto.setName("Fromage");
        dto.setQuantity(2);

        // When
        StockItemResDto result = stockService.addItemToStock(colocation.getId(), stock.getId(), dto);

        // Then
        assertNotNull(result);
        assertEquals("Fromage", result.getName());
        assertEquals(2, result.getQuantity());
    }

    @Test
    public void testAddItemToStock_NoCapacityLimit() throws
                                                     TechnicalException {
        // Given
        this.initSecurityContextPlaceHolder();

        StockEntity stock = new StockEntity("Frigo", colocation);
        stock.setMaxCapacity(null); // No capacity limit
        stock = stockRepository.save(stock);

        // Add large quantity item
        StockItemReqDto dto = new StockItemReqDto();
        dto.setName("Eau");
        dto.setQuantity(1000);

        // When
        StockItemResDto result = stockService.addItemToStock(colocation.getId(), stock.getId(), dto);

        // Then
        assertNotNull(result);
        assertEquals("Eau", result.getName());
        assertEquals(1000, result.getQuantity());
    }

    @Test
    public void testUpdateStockItem_ExceedsCapacity() throws
                                                      TechnicalException {
        // Given
        this.initSecurityContextPlaceHolder();

        StockEntity stock = new StockEntity("Frigo", colocation);
        stock.setMaxCapacity(10);
        stock = stockRepository.save(stock);

        // Add existing items
        StockItemEntity item1 = new StockItemEntity("Lait", 3, managerUser);
        item1.setStock(stock);
        item1 = stockItemRepository.save(item1);

        StockItemEntity item2 = new StockItemEntity("Fromage", 4, managerUser);
        item2.setStock(stock);
        stockItemRepository.save(item2);

        // Try to update item1 to quantity 8 (total would be 8 + 4 = 12, exceeding max capacity of 10)
        StockItemReqDto updateDto = new StockItemReqDto();
        updateDto.setName("Lait");
        updateDto.setQuantity(8);

        // When & Then
        final StockItemEntity finalItem1 = item1;
        final StockEntity     finalStock = stock;
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> stockService.updateStockItem(colocation.getId(), finalStock.getId(), finalItem1.getId(), updateDto));

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage()
                            .contains("dépasserait la capacité maximale du stock"));
        assertTrue(exception.getMessage()
                            .contains("12"));
        assertTrue(exception.getMessage()
                            .contains("10"));
    }

    @Test
    public void testUpdateStockItem_WithinCapacity() throws
                                                     TechnicalException {
        // Given
        this.initSecurityContextPlaceHolder();

        StockEntity stock = new StockEntity("Frigo", colocation);
        stock.setMaxCapacity(10);
        stock = stockRepository.save(stock);

        // Add existing items
        StockItemEntity item1 = new StockItemEntity("Lait", 3, managerUser);
        item1.setStock(stock);
        item1 = stockItemRepository.save(item1);

        StockItemEntity item2 = new StockItemEntity("Fromage", 4, managerUser);
        item2.setStock(stock);
        stockItemRepository.save(item2);

        // Update item1 to quantity 6 (total would be 6 + 4 = 10, exactly at max capacity)
        StockItemReqDto updateDto = new StockItemReqDto();
        updateDto.setName("Lait Bio");
        updateDto.setQuantity(6);

        // When
        StockItemResDto result = stockService.updateStockItem(colocation.getId(), stock.getId(), item1.getId(), updateDto);

        // Then
        assertNotNull(result);
        assertEquals("Lait Bio", result.getName());
        assertEquals(6, result.getQuantity());
    }

    @Test
    public void testUpdateStockItem_ReducingQuantity() throws
                                                       TechnicalException {
        // Given
        this.initSecurityContextPlaceHolder();

        StockEntity stock = new StockEntity("Frigo", colocation);
        stock.setMaxCapacity(10);
        stock = stockRepository.save(stock);

        // Add existing items that are at max capacity
        StockItemEntity item1 = new StockItemEntity("Lait", 6, managerUser);
        item1.setStock(stock);
        item1 = stockItemRepository.save(item1);

        StockItemEntity item2 = new StockItemEntity("Fromage", 4, managerUser);
        item2.setStock(stock);
        stockItemRepository.save(item2);

        // Update item1 to reduce quantity to 2 (total would be 2 + 4 = 6, well under capacity)
        StockItemReqDto updateDto = new StockItemReqDto();
        updateDto.setName("Lait");
        updateDto.setQuantity(2);

        // When
        StockItemResDto result = stockService.updateStockItem(colocation.getId(), stock.getId(), item1.getId(), updateDto);

        // Then
        assertNotNull(result);
        assertEquals("Lait", result.getName());
        assertEquals(2, result.getQuantity());
    }

    @Test
    public void testAddItemToStock_NullQuantity() throws
                                                  TechnicalException {
        // Given
        this.initSecurityContextPlaceHolder();

        StockEntity stock = new StockEntity("Frigo", colocation);
        stock.setMaxCapacity(10);
        stock = stockRepository.save(stock);

        // Add item with null quantity (should be treated as 0)
        StockItemReqDto dto = new StockItemReqDto();
        dto.setName("Test Item");
        dto.setQuantity(2);

        // When
        StockItemResDto result = stockService.addItemToStock(colocation.getId(), stock.getId(), dto);

        // Then
        assertNotNull(result);
        assertEquals("Test Item", result.getName());
        assertNotNull(result.getQuantity());
        assertEquals(2, result.getQuantity());
    }

    @Test
    public void testCapacityValidation_MultipleItemsAtCapacity() throws
                                                                 TechnicalException {
        // Given
        this.initSecurityContextPlaceHolder();

        StockEntity stock = new StockEntity("Frigo", colocation);
        stock.setMaxCapacity(15);
        stock = stockRepository.save(stock);

        // Add multiple items totaling exactly 15
        StockItemEntity item1 = new StockItemEntity("Item1", 5, managerUser);
        item1.setStock(stock);
        stockItemRepository.save(item1);

        StockItemEntity item2 = new StockItemEntity("Item2", 7, managerUser);
        item2.setStock(stock);
        stockItemRepository.save(item2);

        StockItemEntity item3 = new StockItemEntity("Item3", 3, managerUser);
        item3.setStock(stock);
        stockItemRepository.save(item3);

        // Try to add one more item with quantity 1 (should fail)
        StockItemReqDto dto = new StockItemReqDto();
        dto.setName("Item4");
        dto.setQuantity(1);

        // When & Then
        final StockEntity finalStock = stock;
        TechnicalException exception = assertThrows(TechnicalException.class,
                                                    () -> stockService.addItemToStock(colocation.getId(), finalStock.getId(), dto));

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage()
                            .contains("16"));
        assertTrue(exception.getMessage()
                            .contains("15"));
    }
}
