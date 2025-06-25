package Test.LockTestV1.noLock.service;

import Test.LockTestV1.noLock.domain.ProductV1;
import Test.LockTestV1.noLock.repository.ProductRepositoryV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceV1 {

    private final ProductRepositoryV1 productRepository;

    @Transactional
    public boolean decreaseStock(Long productId) {
        ProductV1 product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        log.info("{}개 입니다.", product.getStock());
        if(product.getStock() == 0) return false;
        product.decreaseStock();
        productRepository.saveAndFlush(product);
        return true;
    }
}
