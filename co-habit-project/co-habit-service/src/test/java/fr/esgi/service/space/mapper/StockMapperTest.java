package fr.esgi.service.space.mapper;

import fr.esgi.domain.dto.space.StockItemReqDto;
import fr.esgi.domain.dto.space.StockItemResDto;
import fr.esgi.domain.dto.space.StockReqDto;
import fr.esgi.domain.dto.space.StockResDto;
import fr.esgi.persistence.entity.space.StockEntity;
import fr.esgi.persistence.entity.space.StockItemEntity;
import fr.esgi.service.registration.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockMapperTest {

    private StockMapper mapper;

    @BeforeEach
    void setUp() throws Exception {
        StockMapperImpl stockMapper = new StockMapperImpl();
        UserMapper userMapper = Mappers.getMapper(UserMapper.class);
        Field userMapFields = StockMapperImpl.class.getDeclaredField("userMapper");
        userMapFields.setAccessible(true);
        userMapFields.set(stockMapper, userMapper);
        this.mapper = stockMapper;
    }

    @Test
    void shouldMapDtoToStockEntity() {
        StockReqDto dto = new StockReqDto();
        dto.setTitle("Frigo");
        dto.setColor("Blanc");
        dto.setImageAsset("frigo.png");
        dto.setMaxCapacity(10);

        StockEntity entity = mapper.mapDtoToStock(dto);

        assertNotNull(entity);
        assertNull(entity.getId());
        assertEquals("Frigo", entity.getTitle());
        assertEquals("Blanc", entity.getColor());
        assertEquals("frigo.png", entity.getImageAsset());
        assertEquals(10, entity.getMaxCapacity());
        assertNull(entity.getColocation());
        assertTrue(entity.getItems().isEmpty());
    }

    @Test
    void shouldMapStockEntityToResDto_andCalculateItemCount() {
        StockEntity stock = new StockEntity();
        stock.setId(5L);
        stock.setTitle("Placard");
        stock.setColor("Bois");
        stock.setMaxCapacity(20);

        // add 3 items
        StockItemEntity i1 = new StockItemEntity();
        i1.setQuantity(2);
        stock.addItem(i1);

        StockItemEntity i2 = new StockItemEntity();
        i2.setQuantity(3);
        stock.addItem(i2);

        StockItemEntity i3 = new StockItemEntity();
        i3.setQuantity(null);
        stock.addItem(i3);

        StockResDto res = mapper.mapStockToResDto(stock);

        assertNotNull(res);
        assertEquals(5L, res.getId());
        assertEquals("Placard", res.getTitle());
        assertEquals("Bois", res.getColor());
        assertEquals(20, res.getMaxCapacity());
        // 2 + 3 + (null→0) = 5
        assertEquals(5, res.getItemCount());
    }

    @Test
    void shouldMapListOfStocksToResDtos() {
        StockEntity s1 = new StockEntity();
        s1.setId(1L);
        StockEntity s2 = new StockEntity();
        s2.setId(2L);

        List<StockResDto> list = mapper.mapStocksToResDtos(List.of(s1, s2));
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(1L, list.get(0).getId());
        assertEquals(2L, list.get(1).getId());
    }

    @Test
    void shouldUpdateStockEntityFromDto() {
        StockReqDto dto = new StockReqDto();
        dto.setTitle("Nouveau titre");
        dto.setColor("Rouge");
        dto.setImageAsset("nouveau.png");
        dto.setMaxCapacity(50);

        StockEntity existing = new StockEntity();
        existing.setId(9L);
        existing.setTitle("Ancien");
        existing.setColor("Vert");
        existing.setImageAsset("ancien.png");
        existing.setMaxCapacity(30);

        // add an item to ensure we don't touch the list
        StockItemEntity item = new StockItemEntity();
        item.setQuantity(1);
        existing.addItem(item);

        mapper.updateStockFromDto(dto, existing);

        assertEquals(9L, existing.getId());
        assertEquals("Nouveau titre", existing.getTitle());
        assertEquals("Rouge", existing.getColor());
        assertEquals("nouveau.png", existing.getImageAsset());
        assertEquals(50, existing.getMaxCapacity());
        assertEquals(1, existing.getItems().size());
    }

    @Test
    void shouldMapDtoToStockItemEntity() {
        StockItemReqDto dto = new StockItemReqDto();
        dto.setName("Lait");
        dto.setQuantity(2);

        StockItemEntity entity = mapper.mapDtoToStockItem(dto);

        assertNotNull(entity);
        assertNull(entity.getId());
        assertEquals("Lait", entity.getName());
        assertEquals(2, entity.getQuantity());
        assertNull(entity.getStock());
        assertNull(entity.getAddedBy());
        assertNull(entity.getAddedAt());
    }

    @Test
    void shouldMapStockItemEntityToResDto() {
        StockItemEntity entity = new StockItemEntity();
        entity.setId(7L);
        entity.setName("Pain");
        entity.setQuantity(3);

        // emulate adding a user
        var user = new fr.esgi.persistence.entity.user.User();
        user.setId(4L);
        user.setFirstName("Jean");
        user.setLastName("Dupont");
        entity.setAddedBy(user);

        var now = java.time.LocalDateTime.of(2025, 6, 10, 9, 0);
        entity.setAddedAt(now);

        StockItemResDto res = mapper.mapStockItemToResDto(entity);

        assertNotNull(res);
        assertEquals(7L, res.getId());
        assertEquals("Pain", res.getName());
        assertEquals(3, res.getQuantity());
        assertNotNull(res.getAddedBy());
        assertEquals("Jean", res.getAddedBy().getFirstName());
        assertEquals("Dupont", res.getAddedBy().getLastName());
    }

    @Test
    void shouldMapListOfStockItemsToResDtos() {
        StockItemEntity a = new StockItemEntity(); a.setId(1L);
        StockItemEntity b = new StockItemEntity(); b.setId(2L);
        List<StockItemResDto> dtos = mapper.mapStockItemsToResDtos(List.of(a, b));
        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        assertEquals(1L, dtos.get(0).getId());
        assertEquals(2L, dtos.get(1).getId());
    }

    @Test
    void shouldUpdateStockItemEntityFromDto() {
        StockItemReqDto dto = new StockItemReqDto();
        dto.setName("Beurre");
        dto.setQuantity(5);

        StockItemEntity existing = new StockItemEntity();
        existing.setId(8L);
        existing.setName("Ancien");
        existing.setQuantity(1);
        existing.setAddedBy(new fr.esgi.persistence.entity.user.User());
        existing.setAddedAt(java.time.LocalDateTime.now());

        mapper.updateStockItemFromDto(dto, existing);

        assertEquals(8L, existing.getId());
        assertEquals("Beurre", existing.getName());
        assertEquals(5, existing.getQuantity());
        // addedBy et addedAt mustn't change
        assertNotNull(existing.getAddedBy());
        assertNotNull(existing.getAddedAt());
    }

    @Test
    void shouldHandleNullCalculateTotalQuantity() {
        StockEntity empty = new StockEntity();
        // items null or empty → 0
        empty.setItems(null);
        assertEquals(0, mapper.calculateTotalQuantity(empty));
        empty.setItems(List.of());
        assertEquals(0, mapper.calculateTotalQuantity(empty));
    }
}
