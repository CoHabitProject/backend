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
import fr.esgi.service.AbstractService;
import fr.esgi.service.space.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StockService extends AbstractService {

    private final StockRepository      stockRepository;
    private final StockItemRepository  stockItemRepository;
    private final ColocationRepository colocationRepository;
    private final UserRepository       userRepository;
    private final StockMapper          stockMapper;

    /**
     * Creates a new stock for a colocation
     */
    public StockResDto createStock(Long colocationId, StockReqDto dto) throws
                                                                       TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        Colocation colocation = colocationRepository.findById(colocationId)
                                                    .orElseThrow(() -> new TechnicalException(404, "Colocation non trouvée"));

        if (!colocation.isRoommate(user) && !colocation.isManager(user)) {
            throw new TechnicalException(403, "Vous devez être membre de cette colocation");
        }

        if (stockRepository.existsByTitleAndColocationId(dto.getTitle(), colocationId)) {
            throw new TechnicalException(409, "Un stock avec ce titre existe déjà dans cette colocation");
        }

        StockEntity stock = stockMapper.mapDtoToStock(dto);
        stock.setColocation(colocation);

        StockEntity savedStock = stockRepository.save(stock);
        return stockMapper.mapStockToResDto(savedStock);
    }

    /**
     * Updates an existing stock
     */
    public StockResDto updateStock(Long colocationId, Long stockId, StockReqDto dto) throws
                                                                                     TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        Colocation colocation = colocationRepository.findById(colocationId)
                                                    .orElseThrow(() -> new TechnicalException(404, "Colocation non trouvée"));

        if (!colocation.isRoommate(user) && !colocation.isManager(user)) {
            throw new TechnicalException(403, "Vous devez être membre de cette colocation");
        }

        StockEntity stock = stockRepository.findByIdAndColocationId(stockId, colocationId)
                                           .orElseThrow(() -> new TechnicalException(404, "Stock non trouvé"));

        stockMapper.updateStockFromDto(dto, stock);
        StockEntity updatedStock = stockRepository.save(stock);
        return stockMapper.mapStockToResDto(updatedStock);
    }

    /**
     * Gets all stocks for a colocation
     */
    @Transactional(readOnly = true)
    public List<StockResDto> getStocksByColocation(Long colocationId) throws
                                                                      TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        Colocation colocation = colocationRepository.findById(colocationId)
                                                    .orElseThrow(() -> new TechnicalException(404, "Colocation non trouvée"));

        if (!colocation.isRoommate(user) && !colocation.isManager(user)) {
            throw new TechnicalException(403, "Vous devez être membre de cette colocation");
        }

        List<StockEntity> stocks = stockRepository.findByColocationId(colocationId);
        return stockMapper.mapStocksToResDtos(stocks);
    }

    /**
     * Gets a specific stock by ID
     */
    @Transactional(readOnly = true)
    public StockResDto getStockById(Long colocationId, Long stockId) throws
                                                                     TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        Colocation colocation = colocationRepository.findById(colocationId)
                                                    .orElseThrow(() -> new TechnicalException(404, "Colocation non trouvée"));

        if (!colocation.isRoommate(user) && !colocation.isManager(user)) {
            throw new TechnicalException(403, "Vous devez être membre de cette colocation");
        }

        StockEntity stock = stockRepository.findByIdAndColocationId(stockId, colocationId)
                                           .orElseThrow(() -> new TechnicalException(404, "Stock non trouvé"));

        return stockMapper.mapStockToResDto(stock);
    }

    /**
     * Deletes a stock
     */
    public void deleteStock(Long colocationId, Long stockId) throws
                                                             TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        Colocation colocation = colocationRepository.findById(colocationId)
                                                    .orElseThrow(() -> new TechnicalException(404, "Colocation non trouvée"));

        if (!colocation.isManager(user)) {
            throw new TechnicalException(403, "Seul le gestionnaire peut supprimer un stock");
        }

        StockEntity stock = stockRepository.findByIdAndColocationId(stockId, colocationId)
                                           .orElseThrow(() -> new TechnicalException(404, "Stock non trouvé"));

        stockRepository.delete(stock);
    }

    /**
     * Deletes a stock by ID with all its items
     */
    public void deleteStockById(Long stockId) throws
                                              TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        StockEntity stock = stockRepository.findById(stockId)
                                           .orElseThrow(() -> new TechnicalException(404, "Stock non trouvé"));

        Colocation colocation = stock.getColocation();

        if (!colocation.isManager(user)) {
            throw new TechnicalException(403, "Seul le gestionnaire peut supprimer un stock");
        }

        // Delete all items first (if needed for audit or custom logic)
        List<StockItemEntity> items = stockItemRepository.findByStockId(stockId);
        stockItemRepository.deleteAll(items);

        // Delete the stock (cascade will also delete items)
        stockRepository.delete(stock);
    }

    /**
     * Calculates the current total quantity of items in a stock
     */
    private int getCurrentTotalQuantity(Long stockId) {
        List<StockItemEntity> items = stockItemRepository.findByStockId(stockId);
        return items.stream()
                   .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                   .sum();
    }

    /**
     * Validates if adding the given quantity would exceed the stock's max capacity
     */
    private void validateCapacity(StockEntity stock, int additionalQuantity, Long excludeItemId) throws TechnicalException {
        if (stock.getMaxCapacity() == null) {
            return; // No capacity limit
        }

        int currentTotal = getCurrentTotalQuantity(stock.getId());
        
        // If we're updating an existing item, subtract its current quantity
        if (excludeItemId != null) {
            StockItemEntity existingItem = stockItemRepository.findById(excludeItemId).orElse(null);
            if (existingItem != null && existingItem.getQuantity() != null) {
                currentTotal -= existingItem.getQuantity();
            }
        }

        int newTotal = currentTotal + additionalQuantity;
        
        if (newTotal > stock.getMaxCapacity()) {
            throw new TechnicalException(400, 
                String.format("La quantité totale (%d) dépasserait la capacité maximale du stock (%d)", 
                             newTotal, stock.getMaxCapacity()));
        }
    }

    /**
     * Adds an item to a stock
     */
    public StockItemResDto addItemToStock(Long colocationId, Long stockId, StockItemReqDto dto) throws
                                                                                                TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        Colocation colocation = colocationRepository.findById(colocationId)
                                                    .orElseThrow(() -> new TechnicalException(404, "Colocation non trouvée"));

        if (!colocation.isRoommate(user) && !colocation.isManager(user)) {
            throw new TechnicalException(403, "Vous devez être membre de cette colocation");
        }

        StockEntity stock = stockRepository.findByIdAndColocationId(stockId, colocationId)
                                           .orElseThrow(() -> new TechnicalException(404, "Stock non trouvé"));

        // Validate capacity before adding
        int quantityToAdd = dto.getQuantity() != null ? dto.getQuantity() : 0;
        validateCapacity(stock, quantityToAdd, null);

        StockItemEntity stockItem = stockMapper.mapDtoToStockItem(dto);
        stockItem.setStock(stock);
        stockItem.setAddedBy(user);

        StockItemEntity savedItem = stockItemRepository.save(stockItem);
        return stockMapper.mapStockItemToResDto(savedItem);
    }

    /**
     * Updates a stock item
     */
    public StockItemResDto updateStockItem(Long colocationId, Long stockId, Long itemId, StockItemReqDto dto) throws
                                                                                                              TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        Colocation colocation = colocationRepository.findById(colocationId)
                                                    .orElseThrow(() -> new TechnicalException(404, "Colocation non trouvée"));

        if (!colocation.isRoommate(user) && !colocation.isManager(user)) {
            throw new TechnicalException(403, "Vous devez être membre de cette colocation");
        }

        StockItemEntity stockItem = stockItemRepository.findByIdAndStockId(itemId, stockId)
                                                       .orElseThrow(() -> new TechnicalException(404, "Item non trouvé"));

        StockEntity stock = stockItem.getStock();
        
        // Validate capacity before updating
        int newQuantity = dto.getQuantity() != null ? dto.getQuantity() : 0;
        validateCapacity(stock, newQuantity, itemId);

        stockMapper.updateStockItemFromDto(dto, stockItem);
        StockItemEntity updatedItem = stockItemRepository.save(stockItem);
        return stockMapper.mapStockItemToResDto(updatedItem);
    }

    /**
     * Gets all items for a stock
     */
    @Transactional(readOnly = true)
    public List<StockItemResDto> getStockItems(Long colocationId, Long stockId) throws
                                                                                TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        Colocation colocation = colocationRepository.findById(colocationId)
                                                    .orElseThrow(() -> new TechnicalException(404, "Colocation non trouvée"));

        if (!colocation.isRoommate(user) && !colocation.isManager(user)) {
            throw new TechnicalException(403, "Vous devez être membre de cette colocation");
        }

        stockRepository.findByIdAndColocationId(stockId, colocationId)
                       .orElseThrow(() -> new TechnicalException(404, "Stock non trouvé"));

        List<StockItemEntity> items = stockItemRepository.findByStockId(stockId);
        return stockMapper.mapStockItemsToResDtos(items);
    }

    /**
     * Deletes a stock item
     */
    public void deleteStockItem(Long colocationId, Long stockId, Long itemId) throws
                                                                              TechnicalException {
        String userSub = getUserSub();

        User user = userRepository.findByKeyCloakSub(userSub)
                                  .orElseThrow(() -> new TechnicalException(404, "Utilisateur n'est pas trouvé"));

        Colocation colocation = colocationRepository.findById(colocationId)
                                                    .orElseThrow(() -> new TechnicalException(404, "Colocation non trouvée"));

        if (!colocation.isRoommate(user) && !colocation.isManager(user)) {
            throw new TechnicalException(403, "Vous devez être membre de cette colocation");
        }

        StockItemEntity stockItem = stockItemRepository.findByIdAndStockId(itemId, stockId)
                                                       .orElseThrow(() -> new TechnicalException(404, "Item non trouvé"));

        stockItemRepository.delete(stockItem);
    }
}
