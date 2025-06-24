package Test.LockTestV1.noLock.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class ProductV1 {

    @Id
    private Long id;

    private String name;

    private int stock;

    protected ProductV1() {}

    public ProductV1(Long id, String name, int stock) {
        this.id = id;
        this.name = name;
        this.stock = stock;
    }
}
