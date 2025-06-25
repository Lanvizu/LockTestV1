package Test.LockTestV1.optimisticLock.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductV2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int stock;

    @Version
    private Long version;

    public ProductV2(String name, int stock) {
        this.name = name;
        this.stock = stock;
    }

    public void decreaseStock() {
        this.stock -= 1;
    }
}
