package com.example.simplebooking;

import com.example.simplebooking.seat.Seat;
import com.example.simplebooking.seat.SeatRepository;
import com.example.simplebooking.seat.SeatService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SeatServiceTest {

    @Autowired
    SeatRepository seatRepository;

    @Resource
    SeatService seatService;

    @Autowired
    MockMvc mockMvc;

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

    /**
     *  원자성 테스트
     *  50개 스레드에서 동시에 요청을 날려서 1개만 업데이트에 성공하는지 확인한다
     *
     */
    @Test
    void reserveAtomic_endpoint_allowsOnlyOneCreated_underConcurrency() throws Exception {
        int threads = 50;

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        // 각 요청의 HTTP status를 모을 리스트
        List<Future<Integer>> futures = new java.util.ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                ready.countDown();
                start.await();

                var result = mockMvc.perform(post("/reserve-atomic/" + seatId)
                                .contentType("application/json")
                                .content("{}"))
                        .andReturn();

                return result.getResponse().getStatus(); // 201 / 409 / ...
            }));
        }

        assertTrue(ready.await(5, TimeUnit.SECONDS));
        start.countDown();

        int created = 0;
        int conflict = 0;
        int other = 0;

        for (Future<Integer> f : futures) {
            int status = f.get(10, TimeUnit.SECONDS);
            if (status == 201) created++;
            else if (status == 409) conflict++;
            else other++;
        }

        pool.shutdownNow();

        // 기대값: 1건만 성공(201), 나머지는 충돌(409)
        assertEquals(1, created, "Only one request should create a reservation");
        assertEquals(threads - 1, conflict, "All other requests should be conflict");
        assertEquals(0, other, "Unexpected statuses present");
    }
}