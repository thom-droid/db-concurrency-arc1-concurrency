package com.example.simplebooking.seat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SeatService {
    
    private final SeatRepository seatRepository;

    /**
     * <p>
     *     seatId를 가진 seat를 예약한다.
     *     여러 스레드가 테이블의 로우에 접근하는 race condition을 구현한다.
     *     이 때 최초로 업데이트를 성공한 트랜잭션 이전에 스냅샷을 생성한 트랜잭션이 있다면,해당 트랜잭션에서는
     *     오래된 스냅샷을 보고 있기 때문에 reserved = false를 읽게 되고, 따라서 update를 실행하게 된다.
     * </p>
     *
     * <p>
     *     해당 프로젝트에서는 PostgreSQL를 사용하고 있기 때문에 같은 row에 대한 업데이트에 exclusive lock이 걸리게 되지만
     *     결과적으로는 순차적으로 해당 트랜잭션들에 모두 update가 실행되게 된다.
     * </p>
     */
    @Transactional
    public boolean reserveUnsafe(Long seatId) {
        Seat seat = seatRepository.findById(seatId).orElseThrow(); // select. snapshot

        if (!seat.isReserved()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            seat.setReserved(true);
            seatRepository.save(seat); // update
            return true;
        }
        return false;
    }

}
