package Test.LockTestV1.jvmLock.repository;

import Test.LockTestV1.jvmLock.domain.ProductV3;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepositoryV3 extends JpaRepository<ProductV3, Long> {
}
