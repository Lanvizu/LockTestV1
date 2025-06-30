package Test.LockTestV1.pessimisticLock.service;

import Test.LockTestV1.pessimisticLock.domain.ProductV4;
import Test.LockTestV1.pessimisticLock.repository.ProductRepositoryV4;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceV4 {

    private final ProductRepositoryV4 productRepositoryV4;

    @Transactional
    public boolean decreaseStockWithPessimisticLock(Long productId) {
        ProductV4 product = productRepositoryV4.findByIdWithPessimisticLock(productId).orElseThrow();
        if (product.getStock() <= 0) return false;
        product.decreaseStock();
        return true;
    }

    @Transactional
    public ProductV4 findById(Long productId) {
        return productRepositoryV4.findById(productId).orElseThrow();
    }
}
