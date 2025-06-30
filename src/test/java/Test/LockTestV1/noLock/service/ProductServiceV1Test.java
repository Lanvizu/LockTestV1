package Test.LockTestV1.noLock.service;

import Test.LockTestV1.noLock.domain.ProductV1;
import Test.LockTestV1.noLock.repository.ProductRepositoryV1;
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
class ProductServiceV1Test {

    @Autowired
    private ProductRepositoryV1 productRepository;

    @Autowired
    private ProductServiceV1 productService;

    private static final int INITIAL_STOCK = 100;
    private static Long PRODUCT_ID;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        ProductV1 product = productRepository.save(new ProductV1("테스트 상품", INITIAL_STOCK));
        PRODUCT_ID = product.getId();
    }

    @Test
    @DisplayName("NoLock - 동시에 100명 재고 감소 테스트")
    @PerformanceMeasure("NoLock - 동시에 100명 재고 감소 테스트")
    void decreaseStockConcurrently() throws InterruptedException {
        //given
        final int TOTAL_USERS = 100;
        CountDownLatch latch = new CountDownLatch(TOTAL_USERS);
        AtomicInteger successCount = new AtomicInteger(0);

        //when
        List<Thread> threads = IntStream.range(0, TOTAL_USERS)
                .mapToObj(i -> new Thread(new StockDecreaseWorker(productService, PRODUCT_ID, latch, successCount)))
                .collect(Collectors.toList());

        threads.forEach(Thread::start);
        latch.await();

        //then
        int finalStock = productRepository.findById(PRODUCT_ID).orElseThrow().getStock();
//        System.out.println("성공적으로 감소된 횟수: " + successCount.get());
//        System.out.println("최종 재고: " + finalStock);

        // 하지만 실제 재고는 모두 반영하지 못함 -> 동시성 제어 실패
        assertThat(finalStock).isLessThanOrEqualTo(INITIAL_STOCK);
        assertThat(finalStock).isGreaterThan(0);
    }

    static class StockDecreaseWorker implements Runnable {

        private final ProductServiceV1 productService;
        private final Long productId;
        private final CountDownLatch latch;
        private final AtomicInteger successCount;

        public StockDecreaseWorker(ProductServiceV1 productService, Long productId,
                                   CountDownLatch latch, AtomicInteger successCount) {
            this.productService = productService;
            this.productId = productId;
            this.latch = latch;
            this.successCount = successCount;
        }

        @Override
        public void run() {
            try {
                if (productService.decreaseStock(productId)) {
                    successCount.incrementAndGet();
                }
            } finally {
                latch.countDown();
            }
        }
    }
}