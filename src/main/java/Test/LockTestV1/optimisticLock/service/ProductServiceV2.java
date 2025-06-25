package Test.LockTestV1.optimisticLock.service;

import Test.LockTestV1.optimisticLock.domain.ProductV2;
import Test.LockTestV1.optimisticLock.repository.ProductRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceV2 {

    private final ProductRepositoryV2 productRepositoryV2;

    @Transactional
    public boolean decreaseStockByOptimisticLock(Long productId) {
        try {
            ProductV2 product = productRepositoryV2.findById(productId).orElseThrow();
            log.info("{}개 입니다.", product.getStock());
            if (product.getStock() == 0) {
                return false;
            }
            product.decreaseStock();
            // save()는 불필요 (변경 감지)
            return true;
        } catch (ObjectOptimisticLockingFailureException e) {
            // 예외 발생 시 false 반환
            return false;
        }
    }

    public ProductV2 findById(Long productId) {
        return productRepositoryV2.findById(productId).orElseThrow();
    }
}
