package fr.esgi.persistence.repository.space;

import fr.esgi.persistence.entity.space.StockItemEntity;
import fr.esgi.persistence.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockItemRepository extends JpaRepository<StockItemEntity, Long> {

    List<StockItemEntity> findByStockId(Long stockId);

    Optional<StockItemEntity> findByIdAndStockId(Long id, Long stockId);

    List<StockItemEntity> findByNameContainingIgnoreCaseAndStockId(String name, Long stockId);

    @Query("SELECT si FROM StockItemEntity si WHERE si.stock.id = :stockId AND si.quantity > 0")
    List<StockItemEntity> findAvailableItemsByStockId(@Param("stockId") Long stockId);

    @Query("SELECT si FROM StockItemEntity si WHERE si.stock.id = :stockId AND si.quantity = 0")
    List<StockItemEntity> findOutOfStockItemsByStockId(@Param("stockId") Long stockId);

    @Query("SELECT si FROM StockItemEntity si WHERE si.stock.colocation.id = :colocationId")
    List<StockItemEntity> findByColocationId(@Param("colocationId") Long colocationId);

    @Query("SELECT si FROM StockItemEntity si WHERE si.stock.colocation.id = :colocationId AND si.name LIKE %:name%")
    List<StockItemEntity> findByNameContainingAndColocationId(@Param("name") String name, @Param("colocationId") Long colocationId);

    @Query("SELECT SUM(si.quantity) FROM StockItemEntity si WHERE si.stock.id = :stockId")
    Integer getTotalQuantityByStockId(@Param("stockId") Long stockId);

    boolean existsByNameAndStockId(String name, Long stockId);

    List<StockItemEntity> findByAddedBy(User user);

    List<StockItemEntity> findByStockIdAndAddedBy(Long stockId, User user);

    @Query("SELECT si FROM StockItemEntity si WHERE si.stock.colocation.id = :colocationId AND si.addedBy = :user")
    List<StockItemEntity> findByColocationIdAndAddedBy(@Param("colocationId") Long colocationId, @Param("user") User user);

    @Query("SELECT COUNT(si) FROM StockItemEntity si WHERE si.addedBy = :user")
    Long countByAddedBy(@Param("user") User user);

    void deleteByIdAndStockId(Long id, Long stockId);
}
