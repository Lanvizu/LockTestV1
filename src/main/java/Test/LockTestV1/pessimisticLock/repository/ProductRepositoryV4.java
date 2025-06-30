package Test.LockTestV1.pessimisticLock.repository;

import Test.LockTestV1.pessimisticLock.domain.ProductV4;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepositoryV4 extends JpaRepository<ProductV4, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductV4 p where p.id = :id")
    Optional<ProductV4> findByIdWithPessimisticLock(long id);
}
