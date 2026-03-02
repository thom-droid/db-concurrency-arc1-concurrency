package com.example.simplebooking.seat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class SeatService {

    private final SeatRepository seatRepository;

    /**
     * <p>
     * seatId를 가진 seat를 예약한다.
     * 여러 스레드가 테이블의 로우에 접근하는 race condition을 구현한다.
     * 이 때 최초로 업데이트를 성공한 트랜잭션 이전에 스냅샷을 생성한 트랜잭션이 있다면,해당 트랜잭션에서는
     * 오래된 스냅샷을 보고 있기 때문에 reserved = false를 읽게 되고, 따라서 update를 실행하게 된다.
     * </p>
     *
     * <p>
     * 해당 프로젝트에서는 PostgreSQL를 사용하고 있기 때문에 같은 row에 대한 업데이트에 exclusive lock이 걸리게 되지만
     * 결과적으로는 순차적으로 트랜잭션들이 모두 update를 실행하게 된다.
     * </p>
     * <p>
     * 비즈니스 로직 결과만 보면 트랜잭션이 성공한 것으로 보이지만, 사실 여러 트랜잭션이 같은 row에 대해
     * update를 함으로써 덮어쓰기가 된 것이다.
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

    /**
     * check-then-act 방식이 아닌, atomic update
     * db값을 비즈니스 로직에서 확인하고 업데이트를 실행하는 것이 아니라 쿼리 상으로 조건에 부합할 때만 업데이트
     * 이는 check 와 act 사이에 발생할 수 있는 time gap을 없애기 위함
     */
    @Transactional
    public boolean
    reserveAtomic(Long seatId) {
        int updated = seatRepository.reserveIfAvailable(seatId);
        return updated == 1;
    }

    /**
     * <p>
     * 트랜잭션 범위를 설정하지 않고 비즈니스 로직을 수행
     * </p>
     *
     */
    public boolean reserveUnsafeWithoutTransactional(Long seatId) {
        Seat seat = seatRepository.findById(seatId).orElseThrow(NoSuchElementException::new);
        boolean reserved = seat.isReserved();
        if (!reserved) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            seat.setReserved(true);
            seatRepository.save(seat); // update
            return true;
        } else {
            throw new IllegalStateException("already reserved!");
        }
    }

    /**
     * <p>
     *     {@link Transactional}는 프록시를 생성하여 클래스 내에서 메서드가 실행될 때 인터셉트하여 메서드 수행을 트랜잭션에 참여시킨다.
     * </p>
     * <p>
     *     그런데 {@code this.innerTx()}는 클래스 내에 다른 메서드를 호출할 뿐이라 프록시가 감지할 수 없기 때문에 트랜잭션 바운더리에
     *     걸리지 않는다. 즉 이 메서드를 수행하면 원자성이 보장되지 않는다.
     * </p>
     *
     *
     */
    public void outerCall(Long seatId) {
        this.innerTx(seatId);
    }

    @Transactional
    public void innerTx(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(NoSuchElementException::new);

        seat.setReserved(true);
        seatRepository.save(seat);

        // Force rollback if transaction is active
        throw new RuntimeException("boom");
    }
}
