package fr.esgi.service.space.mapper;

import fr.esgi.domain.dto.space.StockItemReqDto;
import fr.esgi.domain.dto.space.StockItemResDto;
import fr.esgi.domain.dto.space.StockReqDto;
import fr.esgi.domain.dto.space.StockResDto;
import fr.esgi.persistence.entity.space.Colocation;
import fr.esgi.persistence.entity.space.StockEntity;
import fr.esgi.persistence.entity.space.StockItemEntity;
import fr.esgi.persistence.entity.user.User;
import fr.esgi.service.registration.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StockMapperTest {

    private StockMapper mapper;

    private StockReqDto stockReqDto;
    private StockEntity stockEntity;

    private StockItemReqDto itemReqDto;
    private StockItemEntity itemEntity;

    private User addedBy;

    @BeforeEach
    void setUp() throws Exception {
        mapper = Mappers.getMapper(StockMapper.class);

        UserMapper userMapper = Mappers.getMapper(UserMapper.class);
        // via reflection car le champ est privé
        Field f = mapper.getClass().getDeclaredField("userMapper");
        f.setAccessible(true);
        f.set(mapper, userMapper);

        // --- StockReqDto
        stockReqDto = new StockReqDto();
        stockReqDto.setTitle("Frigo");
        stockReqDto.setImageAsset("fridge.png");
        stockReqDto.setColor("#FF5733");
        stockReqDto.setMaxCapacity(50);

        // --- StockEntity
        stockEntity = new StockEntity();
        stockEntity.setId(10L);
        stockEntity.setTitle("Ancien titre");
        stockEntity.setImageAsset("old.png");
        stockEntity.setColor("#000000");
        stockEntity.setMaxCapacity(20);
        // on simule une colocation attachée
        stockEntity.setColocation(new Colocation());
        // on ajoute quelques items
        StockItemEntity e1 = new StockItemEntity();
        e1.setQuantity(5);
        StockItemEntity e2 = new StockItemEntity();
        e2.setQuantity(10);
        stockEntity.getItems().addAll(Arrays.asList(e1, e2));

        // --- StockItemReqDto
        itemReqDto = new StockItemReqDto();
        itemReqDto.setName("Lait");
        itemReqDto.setQuantity(2);

        // --- StockItemEntity
        itemEntity = new StockItemEntity();
        itemEntity.setId(100L);
        itemEntity.setName("Beurre");
        itemEntity.setQuantity(3);
        addedBy = new User();
        addedBy.setId(1L);
        addedBy.setFirstName("Paul");
        addedBy.setLastName("Dupont");
        addedBy.setEmail("paul.dupont@test.com");
        itemEntity.setAddedBy(addedBy);
        // addedAt / stock sont gérés par JPA mais ignorés par le mapper
    }

    @Test
    void testMapDtoToStock() {
        StockEntity result = mapper.mapDtoToStock(stockReqDto);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(stockReqDto.getTitle());
        assertThat(result.getImageAsset()).isEqualTo(stockReqDto.getImageAsset());
        assertThat(result.getColor()).isEqualTo(stockReqDto.getColor());
        assertThat(result.getMaxCapacity()).isEqualTo(stockReqDto.getMaxCapacity());

        // champs ignorés
        assertThat(result.getId()).isNull();
        assertThat(result.getColocation()).isNull();
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void testMapDtoToStock_WithNull() {
        StockEntity result = mapper.mapDtoToStock(null);
        assertThat(result).isNull();
    }

    @Test
    void testMapStockToResDto() {
        StockResDto dto = mapper.mapStockToResDto(stockEntity);

        assertThat(dto).isNotNull();
        assertThat(dto.getTitle()).isEqualTo(stockEntity.getTitle());
        assertThat(dto.getImageAsset()).isEqualTo(stockEntity.getImageAsset());
        assertThat(dto.getColor()).isEqualTo(stockEntity.getColor());
        assertThat(dto.getMaxCapacity()).isEqualTo(stockEntity.getMaxCapacity());
        // calculateTotalQuantity : 5 + 10 = 15
        assertThat(dto.getItemCount()).isEqualTo(15);
    }

    @Test
    void testMapStockToResDto_WithNullItems() {
        stockEntity.setItems(null);
        StockResDto dto = mapper.mapStockToResDto(stockEntity);
        assertThat(dto.getItemCount()).isZero();
    }

    @Test
    void testMapStocksToResDtos() {
        StockEntity other = new StockEntity();
        other.setTitle("Armoire");
        other.setItems(new ArrayList<>());
        List<StockResDto> list = mapper.mapStocksToResDtos(Arrays.asList(stockEntity, other));

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getTitle()).isEqualTo("Ancien titre");
        assertThat(list.get(1).getTitle()).isEqualTo("Armoire");
    }

    @Test
    void testMapStocksToResDtos_WithNullList() {
        List<StockResDto> list = mapper.mapStocksToResDtos(null);
        assertThat(list).isNull();
    }

    @Test
    void testUpdateStockFromDto() {
        StockEntity target = new StockEntity();
        target.setId(5L);
        target.setTitle("X");
        target.setImageAsset("x.png");
        target.setColor("#FFFFFF");
        target.setMaxCapacity(10);
        target.setColocation(new Colocation());

        mapper.updateStockFromDto(stockReqDto, target);

        // champs mis à jour
        assertThat(target.getTitle()).isEqualTo("Frigo");
        assertThat(target.getImageAsset()).isEqualTo("fridge.png");
        assertThat(target.getColor()).isEqualTo("#FF5733");
        assertThat(target.getMaxCapacity()).isEqualTo(50);

        // champs ignorés
        assertThat(target.getId()).isEqualTo(5L);
        assertThat(target.getColocation()).isNotNull();
    }

    @Test
    void testMapDtoToStockItem() {
        StockItemEntity e = mapper.mapDtoToStockItem(itemReqDto);

        assertThat(e).isNotNull();
        assertThat(e.getName()).isEqualTo(itemReqDto.getName());
        assertThat(e.getQuantity()).isEqualTo(itemReqDto.getQuantity());

        assertThat(e.getId()).isNull();
        assertThat(e.getStock()).isNull();
        assertThat(e.getAddedBy()).isNull();
        assertThat(e.getAddedAt()).isNull();
    }

    @Test
    void testMapDtoToStockItem_WithNull() {
        assertThat(mapper.mapDtoToStockItem(null)).isNull();
    }

    @Test
    void testMapStockItemToResDto() {
        StockItemResDto dto = mapper.mapStockItemToResDto(itemEntity);
        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo(itemEntity.getName());
        assertThat(dto.getQuantity()).isEqualTo(itemEntity.getQuantity());
        // addedBy doit être mappé via UserMapper
        assertThat(dto.getAddedBy()).isNotNull();
        assertThat(dto.getAddedBy().getFirstName()).isEqualTo("Paul");
    }

    @Test
    void testMapStockItemsToResDtos() {
        List<StockItemResDto> dtos = mapper.mapStockItemsToResDtos(Arrays.asList(itemEntity));
        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getName()).isEqualTo("Beurre");
    }

    @Test
    void testMapStockItemsToResDtos_WithNullList() {
        List<StockItemResDto> dtos = mapper.mapStockItemsToResDtos(null);
        assertThat(dtos).isNull();
    }

    @Test
    void testUpdateStockItemFromDto() {
        StockItemEntity target = new StockItemEntity();
        target.setId(200L);
        target.setName("Ancien");
        target.setQuantity(5);

        mapper.updateStockItemFromDto(itemReqDto, target);

        assertThat(target.getName()).isEqualTo("Lait");
        assertThat(target.getQuantity()).isEqualTo(2);
        // champs ignorés
        assertThat(target.getId()).isEqualTo(200L);
    }
}
