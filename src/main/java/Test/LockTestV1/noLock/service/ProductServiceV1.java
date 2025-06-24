package Test.LockTestV1.noLock.service;

import Test.LockTestV1.noLock.domain.ProductV1;
import Test.LockTestV1.noLock.repository.ProductRepositoryV1;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceV1 {

    private final ProductRepositoryV1 productRepository;

    @Transactional
    public void decreaseStock(Long productId) {
        ProductV1 product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        int stock = product.getStock();
        if (stock > 0) {
            product.setStock(stock - 1);
        }
    }
}
