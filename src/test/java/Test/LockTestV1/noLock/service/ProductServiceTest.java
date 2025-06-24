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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class ProductServiceTest {

    @Autowired
    private ProductRepositoryV1 productRepository;

    @Autowired
    private ProductServiceV1 productService;

    private static final int INITIAL_STOCK = 100;
    private static final Long PRODUCT_ID = 1L;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        productRepository.save(new ProductV1(PRODUCT_ID, "테스트 상품", INITIAL_STOCK));
    }

    @Test
    @DisplayName("NoLock - 동시에 100명 재고 감소 테스트")
    void decreaseStockConcurrently() throws InterruptedException {
        int threadCount = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<Thread> threads = IntStream.range(0, threadCount)
                .mapToObj(i -> new Thread(new StockDecreaseWorker(productService, PRODUCT_ID, latch)))
                .collect(Collectors.toList());

        threads.forEach(Thread::start);
        latch.await();

        int finalStock = productRepository.findById(PRODUCT_ID).orElseThrow().getStock();
        System.out.println("최종 재고: " + finalStock);
        assertThat(finalStock).isGreaterThan(0);
        assertThat(finalStock).isLessThan(INITIAL_STOCK);
    }

    static class StockDecreaseWorker implements Runnable {

        private final ProductServiceV1 productService;
        private final Long productId;
        private final CountDownLatch latch;

        public StockDecreaseWorker(ProductServiceV1 productService, Long productId, CountDownLatch latch) {
            this.productService = productService;
            this.productId = productId;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                productService.decreaseStock(productId);
            } finally {
                latch.countDown();
            }
        }
    }
}