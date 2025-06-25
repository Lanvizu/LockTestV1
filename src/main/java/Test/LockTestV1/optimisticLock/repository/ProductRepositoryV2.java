package Test.LockTestV1.optimisticLock.repository;

import Test.LockTestV1.optimisticLock.domain.ProductV2;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepositoryV2 extends JpaRepository<ProductV2, Long> {
}
