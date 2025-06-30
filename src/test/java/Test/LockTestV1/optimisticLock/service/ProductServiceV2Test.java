package Test.LockTestV1.optimisticLock.service;

import Test.LockTestV1.optimisticLock.domain.ProductV2;
import Test.LockTestV1.optimisticLock.repository.ProductRepositoryV2;
import Test.LockTestV1.performance.PerformanceMeasure;
import Test.LockTestV1.performance.PerformanceMeasureExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ExtendWith(PerformanceMeasureExtension.class)
class ProductServiceV2Test {

    @Autowired
    private ProductRepositoryV2 productRepositoryV2;
    @Autowired
    private ProductServiceV2 productServiceV2;

    private static final int INITIAL_STOCK = 100;
    private static Long PRODUCT_ID;

    @BeforeEach
    void setUp() {
        productRepositoryV2.deleteAll();
        ProductV2 product = productRepositoryV2.save(new ProductV2("테스트 상품", INITIAL_STOCK));
        PRODUCT_ID = product.getId();
    }

    @Test
    @DisplayName("OptimisticLock - 동시에 100명 재고 감소 테스트")
    @PerformanceMeasure("OptimisticLock - 동시에 100명 재고 감소 테스트")
    void decreaseStockConcurrently() throws InterruptedException {
        //given
        final int TOTAL_USERS = 100;
        CountDownLatch latch = new CountDownLatch(TOTAL_USERS);
        AtomicInteger successCount = new AtomicInteger(0);

        // when
        List<Thread> threads = IntStream.range(0, TOTAL_USERS)
                .mapToObj(i -> new Thread(new StockDecreaseWorker(productServiceV2, PRODUCT_ID, latch, successCount)))
                .collect(Collectors.toList());

        threads.forEach(Thread::start);
        latch.await();

        // then
        ProductV2 updateProduct = productServiceV2.findById(PRODUCT_ID);
        assertThat(updateProduct.getStock()).isEqualTo(0);
        assertThat(successCount.get()).isEqualTo(TOTAL_USERS);
    }

    static class StockDecreaseWorker implements Runnable {

        private final ProductServiceV2 productServiceV2;
        private final Long productId;
        private final CountDownLatch latch;
        private final AtomicInteger successCount;

        public StockDecreaseWorker(ProductServiceV2 productServiceV2, Long productId,
                                   CountDownLatch latch, AtomicInteger successCount) {
            this.productServiceV2 = productServiceV2;
            this.productId = productId;
            this.latch = latch;
            this.successCount = successCount;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    boolean result = productServiceV2.decreaseStockByOptimisticLock(productId);
                    if (result) {
                        successCount.incrementAndGet();
                    }
                    break;
                } catch (ObjectOptimisticLockingFailureException e) {
//                    System.out.println("감소 실패2");
                }
            }
            latch.countDown();
        }
    }
}