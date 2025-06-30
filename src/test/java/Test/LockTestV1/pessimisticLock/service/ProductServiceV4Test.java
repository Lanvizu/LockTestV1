package Test.LockTestV1.pessimisticLock.service;

import Test.LockTestV1.performance.PerformanceMeasure;
import Test.LockTestV1.performance.PerformanceMeasureExtension;
import Test.LockTestV1.pessimisticLock.domain.ProductV4;
import Test.LockTestV1.pessimisticLock.repository.ProductRepositoryV4;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ExtendWith(PerformanceMeasureExtension.class)
class ProductServiceV4Test {

    @Autowired
    private ProductRepositoryV4 productRepositoryV4;

    @Autowired
    private ProductServiceV4 productServiceV4;

    private static final int INITIAL_STOCK = 100;
    private static Long PRODUCT_ID;

    @BeforeEach
    void setUp() {
        productRepositoryV4.deleteAll();
        ProductV4 product = productRepositoryV4.save(new ProductV4("테스트 상품", INITIAL_STOCK));
        PRODUCT_ID = product.getId();
    }

    @Test
    @DisplayName("PessimisticLock - 동시에 100명 재고 감소 테스트")
    @PerformanceMeasure("PessimisticLock - 동시에 100명 재고 감소 테스트")
    void decreaseStockConcurrently() throws InterruptedException {
        //given
        final int TOTAL_USERS = 100;

        CountDownLatch latch = new CountDownLatch(TOTAL_USERS);

        // when
        List<Thread> threads = IntStream.range(0, TOTAL_USERS)
                .mapToObj(i -> new Thread(new StockDecreaseWorker(productServiceV4, PRODUCT_ID, latch)))
                .collect(Collectors.toList());

        threads.forEach(Thread::start);
        latch.await();

        // then
        ProductV4 updateProduct = productServiceV4.findById(PRODUCT_ID);
        assertThat(updateProduct.getStock()).isEqualTo(0);
    }

    static class StockDecreaseWorker implements Runnable {

        private final ProductServiceV4 productServiceV4;
        private final Long productId;
        private final CountDownLatch latch;

        public StockDecreaseWorker(ProductServiceV4 productServiceV4, Long productId, CountDownLatch latch) {
            this.productServiceV4 = productServiceV4;
            this.productId = productId;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                boolean result = productServiceV4.decreaseStockWithPessimisticLock(productId);
//                System.out.printf("[%s] %s%n",
//                        Thread.currentThread().getName(),
//                        result ? "재고 감소!" : "재고 없음");
            } catch (Exception e) {
//                System.out.printf("[%s] 예외 발생: %s%n",
//                        Thread.currentThread().getName(), e.getMessage());
            } finally {
                latch.countDown();
            }
        }
    }
}