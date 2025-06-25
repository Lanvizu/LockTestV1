package Test.LockTestV1.jvmLock.service;

import Test.LockTestV1.jvmLock.domain.ProductV3;
import Test.LockTestV1.jvmLock.repository.ProductRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceV3 {

    private final ProductRepositoryV3 productRepositoryV3;

    @Transactional
    public void doDecreaseStock(Long productId) {
        ProductV3 product = productRepositoryV3.findById(productId).orElseThrow();
        log.info("{}개 입니다.", product.getStock());
        product.decreaseStock();
    }

    public ProductV3 findById(Long productId) {
        return productRepositoryV3.findById(productId).orElseThrow();
    }
}
