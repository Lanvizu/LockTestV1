package Test.LockTestV1.jvmLock.service;

import Test.LockTestV1.jvmLock.domain.ProductV3;
import Test.LockTestV1.jvmLock.repository.ProductRepositoryV3;
import Test.LockTestV1.performance.PerformanceMeasure;
import Test.LockTestV1.performance.PerformanceMeasureExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ExtendWith(PerformanceMeasureExtension.class)
class ProductServiceV3Test {

    @Autowired
    private ProductRepositoryV3 productRepositoryV3;

    @Autowired
    private ProductFacade productFacade;

    private static final int INITIAL_STOCK = 100;
    private static Long PRODUCT_ID;

    @BeforeEach
    void setUp() {
        productRepositoryV3.deleteAll();
        ProductV3 product = productRepositoryV3.save(new ProductV3("테스트 상품", INITIAL_STOCK));
        PRODUCT_ID = product.getId();
    }

    @Test
    @DisplayName("JvmLock - 동시에 100명 재고 감소 테스트")
    @PerformanceMeasure("JvmLock - 동시에 100명 재고 감소 테스트")
    void decreaseStockConcurrently() throws InterruptedException {
        // given
        final int TOTAL_USERS = 100;
        CountDownLatch latch = new CountDownLatch(TOTAL_USERS);
        AtomicInteger successCount = new AtomicInteger(0);

        // when
        List<Thread> threads = IntStream.range(0, TOTAL_USERS)
                .mapToObj(i -> new Thread(new StockDecreaseWorker(productFacade, PRODUCT_ID, latch, successCount)))
                .collect(Collectors.toList());

        threads.forEach(Thread::start);
        latch.await();

        // then
        int finalStock = productRepositoryV3.findById(PRODUCT_ID).orElseThrow().getStock();

//        System.out.println("성공적으로 실행한 횟수: " + successCount.get());
//        System.out.println("최종 재고: " + finalStock);

        // 요청과 최종 재고 모두 정상 작동
        assertThat(successCount.get()).isEqualTo(INITIAL_STOCK);
        assertThat(finalStock).isEqualTo(0);
    }

    static class StockDecreaseWorker implements Runnable {

        private final ProductFacade productFacade;
        private final Long productId;
        private final CountDownLatch latch;
        private final AtomicInteger successCount;

        public StockDecreaseWorker(ProductFacade productFacade, Long productId,
                                   CountDownLatch latch, AtomicInteger successCount) {
            this.productFacade = productFacade;
            this.productId = productId;
            this.latch = latch;
            this.successCount = successCount;
        }

        @Override
        public void run() {
            try {
                if (productFacade.decreaseStockByJvmLock(productId)) {
                    successCount.incrementAndGet();
                }
            } finally {
                latch.countDown();
            }
        }
    }

}