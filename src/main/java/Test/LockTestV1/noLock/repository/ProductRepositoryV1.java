package Test.LockTestV1.noLock.repository;

import Test.LockTestV1.noLock.domain.ProductV1;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepositoryV1 extends JpaRepository<ProductV1, Long> {
}
