package fr.esgi.persistence.repository.space;

import fr.esgi.persistence.entity.space.StockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<StockEntity, Long> {

    List<StockEntity> findByColocationId(Long colocationId);

    Optional<StockEntity> findByIdAndColocationId(Long id, Long colocationId);

    List<StockEntity> findByTitleContainingIgnoreCaseAndColocationId(String title, Long colocationId);

    @Query("SELECT s FROM StockEntity s WHERE s.colocation.id = :colocationId AND s.maxCapacity IS NOT NULL")
    List<StockEntity> findStocksWithCapacityByColocationId(@Param("colocationId") Long colocationId);

    @Query("SELECT s FROM StockEntity s WHERE s.colocation.id = :colocationId AND s.color = :color")
    List<StockEntity> findByColocationIdAndColor(@Param("colocationId") Long colocationId, @Param("color") String color);

    boolean existsByTitleAndColocationId(String title, Long colocationId);

    void deleteByIdAndColocationId(Long id, Long colocationId);
}
