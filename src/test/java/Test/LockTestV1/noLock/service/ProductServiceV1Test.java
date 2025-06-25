package Test.LockTestV1.noLock.service;

import Test.LockTestV1.noLock.domain.ProductV1;
import Test.LockTestV1.noLock.repository.ProductRepositoryV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class ProductServiceV1Test {

    @Autowired
    private ProductRepositoryV1 productRepository;

    @Autowired
    private ProductServiceV1 productService;

    private static final int INITIAL_STOCK = 20;
    private static Long PRODUCT_ID;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        ProductV1 product = productRepository.save(new ProductV1("테스트 상품", INITIAL_STOCK));
        PRODUCT_ID = product.getId();
    }

    @Test
    @DisplayName("NoLock - 동시에 20명 재고 감소 테스트")
    void decreaseStockConcurrently() throws InterruptedException {
        int threadCount = 20;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        List<Thread> threads = IntStream.range(0, threadCount)
                .mapToObj(i -> new Thread(new StockDecreaseWorker(productService, PRODUCT_ID, latch, successCount)))
                .collect(Collectors.toList());

        threads.forEach(Thread::start);
        latch.await();

        int finalStock = productRepository.findById(PRODUCT_ID).orElseThrow().getStock();

        System.out.println("성공적으로 감소된 횟수: " + successCount.get());
        System.out.println("최종 재고: " + finalStock);

        // 요청은 모두 문제없이 정상 작동
        assertThat(successCount.get()).isEqualTo(INITIAL_STOCK);

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