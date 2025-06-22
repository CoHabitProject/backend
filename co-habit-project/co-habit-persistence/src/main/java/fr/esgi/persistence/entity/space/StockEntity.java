package fr.esgi.persistence.entity.space;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@NoArgsConstructor
public class StockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String color;

    @Column(name = "image_asset")
    private String imageAsset;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colocation_id", nullable = false)
    private Colocation colocation;

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<StockItemEntity> items = new ArrayList<>();

    public StockEntity(String title, Colocation colocation) {
        this.title      = title;
        this.colocation = colocation;
    }

    public void addItem(StockItemEntity item) {
        items.add(item);
        item.setStock(this);
    }

    public void removeItem(StockItemEntity item) {
        items.remove(item);
        item.setStock(null);
    }

    public int getCurrentItemsCount() {
        return items.stream()
                    .mapToInt(StockItemEntity::getQuantity)
                    .sum();
    }

    public boolean isAtCapacity() {
        return maxCapacity != null && getCurrentItemsCount() >= maxCapacity;
    }
}
