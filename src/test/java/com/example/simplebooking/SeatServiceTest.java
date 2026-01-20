package com.example.simplebooking;

import com.example.simplebooking.seat.Seat;
import com.example.simplebooking.seat.SeatRepository;
import com.example.simplebooking.seat.SeatService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SeatServiceTest {

    @Autowired
    SeatRepository seatRepository;

    @Resource
    SeatService seatService;

    private Long seatId;

    @BeforeEach
    void setUp() {
        // Reset DB state for each test
        seatRepository.deleteAll();

        Seat seat = new Seat();
        seat.setReserved(false);
        seatId = seatRepository.save(seat).getId();
    }

    @Test
    void reserve_naiveImplementation_allowsMultipleSaveCalls_underConcurrency() throws Exception {
        int threads = 50;

        // Count how many times the naive reserve path "thinks" it reserved.
        // With no locking, multiple threads may return true here.
        AtomicInteger reserveSucceeded = new AtomicInteger(0);

        ExecutorService pool = Executors.newFixedThreadPool(threads);

        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                ready.countDown();          // "I'm ready"
                try {
                    start.await();           // wait for the starting gun
                    if (seatService.reserveUnsafe(seatId)) {
                        reserveSucceeded.incrementAndGet(); // 실제로 update 실행된 횟수를 반환
                    }
                } catch (Exception ignored) {
                    // for this test, we only care about concurrency behavior
                } finally {
                    done.countDown();
                }
            });
        }

        // Ensure all threads are ready, then start together
        assertTrue(ready.await(5, TimeUnit.SECONDS), "Threads did not get ready in time");
        start.countDown();

        assertTrue(done.await(10, TimeUnit.SECONDS), "Threads did not finish in time");
        pool.shutdownNow();

        // Final DB state (will likely be true)
        Seat finalSeat = seatRepository.findById(seatId).orElseThrow();
        assertTrue(finalSeat.isReserved(), "Seat should end up reserved");

        // 메서드 수행이 아닌 실제 update 행해진 횟수.
        assertTrue(reserveSucceeded.get() > 1,
                "Expected multiple 'successful' reserves due to race, but got: " + reserveSucceeded.get());
    }
}