package Test.LockTestV1.jvmLock.service;

import Test.LockTestV1.jvmLock.domain.ProductV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductFacade {

    private final ProductServiceV3 productServiceV3;

    private final Object lock = new Object();

    public boolean decreaseStockByJvmLock(Long productId) {
        synchronized (lock) {
            return productServiceV3.doDecreaseStock(productId);
        }
    }
}
