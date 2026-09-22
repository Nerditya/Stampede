package com.stampede.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    private ProductService productService;

    // runs before EVERY @Test method — gives each test a fresh ProductService
    @BeforeEach
    void setUp() {
        productService = new ProductService();
        productService.seed();   // loads the 3 sample products (Nike, Adidas, Puma)
    }

    // -------------------------------------------------------------------------
    // Test 1 — Basic decrement
    // -------------------------------------------------------------------------

    @Test
    void decrementStock_shouldReduceStockByOne() {
        int stockBefore = productService.getLiveStock("p1");   // 100

        boolean result = productService.decrementStock("p1", 1);

        assertTrue(result);                                          // should succeed
        assertEquals(stockBefore - 1, productService.getLiveStock("p1")); // stock should be 99
    }

    // -------------------------------------------------------------------------
    // Test 2 — Sold out
    // -------------------------------------------------------------------------

    @Test
    void decrementStock_shouldReturnFalseWhenSoldOut() {
        // buy all 25 Puma Suedes (smallest stock in seed data)
        for (int i = 0; i < 25; i++) {
            productService.decrementStock("p3", 1);
        }

        // 26th attempt should fail
        boolean result = productService.decrementStock("p3", 1);

        assertFalse(result);                                    // sold out
        assertEquals(0, productService.getLiveStock("p3"));    // stock is exactly 0, not negative
    }

    // -------------------------------------------------------------------------
    // Test 3 — Unknown product
    // -------------------------------------------------------------------------

    @Test
    void decrementStock_shouldReturnFalseForUnknownProduct() {
        boolean result = productService.decrementStock("unknown-id", 1);
        assertFalse(result);
    }

    // -------------------------------------------------------------------------
    // Test 4 — Concurrency (the critical one)
    //
    // 200 threads all try to buy "p1" (stock = 100) simultaneously.
    // Expected: exactly 100 succeed, 100 fail. Stock = 0. Never negative.
    //
    // How CountDownLatch works:
    //   - latch is initialised with count = 200
    //   - each thread calls latch.countDown() when ready (count drops by 1)
    //   - latch.await() blocks until count reaches 0
    //   - this makes all 200 threads start buying at the same instant
    //     instead of one after another — maximum contention
    // -------------------------------------------------------------------------

    @Test
    void decrementStock_shouldNeverOversellUnderConcurrency() throws InterruptedException {
        int threadCount = 200;
        int initialStock = 100; // p1 has 100 items

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount    = new AtomicInteger(0);

        // thread pool with exactly threadCount threads — all ready at once
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // latch holds all threads at the starting line until all are ready
        CountDownLatch startLatch = new CountDownLatch(1);

        // latch to wait for all threads to finish before we assert
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();   // wait at starting line — all threads wait here

                    boolean bought = productService.decrementStock("p1", 1);
                    if (bought) successCount.incrementAndGet();
                    else        failCount.incrementAndGet();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();  // signal this thread is done
                }
            });
        }

        startLatch.countDown();     // drop the flag — all 200 threads race simultaneously
        doneLatch.await();          // wait for all 200 threads to finish
        executor.shutdown();

        // assertions
        assertEquals(initialStock,              successCount.get(),
            "Exactly 100 threads should have succeeded");

        assertEquals(threadCount - initialStock, failCount.get(),
            "Exactly 100 threads should have been rejected");

        assertEquals(0, productService.getLiveStock("p1"),
            "Stock should be exactly 0 — never negative");
    }
}
