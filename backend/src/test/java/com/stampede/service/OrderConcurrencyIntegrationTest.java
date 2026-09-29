package com.stampede.service;

import com.stampede.dto.LineItemRequest;
import com.stampede.exception.OutOfStockException;
import com.stampede.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Fires many concurrent orders at a single product and asserts the database's
 * atomic conditional UPDATE never oversells — the V2 equivalent of the V1
 * AtomicInteger concurrency test, but against a real PostgreSQL instance.
 */
@SpringBootTest
@Testcontainers
class OrderConcurrencyIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void concurrentOrders_neverOversell() throws InterruptedException {
        String productId = "p1";                 // seeded with 100 units by DataSeeder
        int initialStock = productRepository.findById(productId).orElseThrow().getLiveStock();

        int threadCount = 200;
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int userNum = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    orderService.placeOrder("user-" + userNum,
                            List.of(new LineItemRequest(productId, 1)));
                    success.incrementAndGet();
                } catch (OutOfStockException e) {
                    failed.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();   // release all threads at once
        doneLatch.await();
        executor.shutdown();

        int remaining = productRepository.findById(productId).orElseThrow().getLiveStock();

        assertEquals(initialStock, success.get(), "exactly initialStock orders should succeed");
        assertEquals(threadCount - initialStock, failed.get(), "the rest should be rejected");
        assertEquals(0, remaining, "stock must not go negative");
    }
}
