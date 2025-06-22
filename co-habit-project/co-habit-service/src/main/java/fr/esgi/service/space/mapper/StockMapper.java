package fr.esgi.service.space.mapper;

import fr.esgi.domain.dto.space.StockItemReqDto;
import fr.esgi.domain.dto.space.StockItemResDto;
import fr.esgi.domain.dto.space.StockReqDto;
import fr.esgi.domain.dto.space.StockResDto;
import fr.esgi.persistence.entity.space.StockEntity;
import fr.esgi.persistence.entity.space.StockItemEntity;
import fr.esgi.service.registration.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface StockMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "colocation", ignore = true)
    @Mapping(target = "items", ignore = true)
    StockEntity mapDtoToStock(StockReqDto dto);

    @Mapping(target = "itemCount", source = ".", qualifiedByName = "calculateTotalQuantity")
    StockResDto mapStockToResDto(StockEntity stock);

    List<StockResDto> mapStocksToResDtos(List<StockEntity> stocks);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "colocation", ignore = true)
    @Mapping(target = "items", ignore = true)
    void updateStockFromDto(StockReqDto dto, @MappingTarget StockEntity stock);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "stock", ignore = true)
    @Mapping(target = "addedBy", ignore = true)
    @Mapping(target = "addedAt", ignore = true)
    StockItemEntity mapDtoToStockItem(StockItemReqDto dto);

    @Mapping(target = "addedBy", source = "addedBy")
    StockItemResDto mapStockItemToResDto(StockItemEntity stockItem);

    List<StockItemResDto> mapStockItemsToResDtos(List<StockItemEntity> stockItems);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "stock", ignore = true)
    @Mapping(target = "addedBy", ignore = true)
    @Mapping(target = "addedAt", ignore = true)
    void updateStockItemFromDto(StockItemReqDto dto, @MappingTarget StockItemEntity stockItem);

    @Named("calculateTotalQuantity")
    default int calculateTotalQuantity(StockEntity stock) {
        if (stock.getItems() == null) {
            return 0;
        }

        return stock.getItems()
                    .stream()
                    .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                    .sum();
    }
}
