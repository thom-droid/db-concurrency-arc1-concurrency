package com.example.simplebooking.seat;

import com.example.simplebooking.user.User;
import com.example.simplebooking.user.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<SeatView> listSeats() {
        return seatRepository.findAll().stream()
            .map(SeatView::new)
            .toList();
    }


    /**
     * 자리를 예약한다.
     * 목표 로우를 락 없이 읽어와 수정하므로 동시에 하나의 로우를 수정할 수 있다.
     * race condition을 구현하기 위해 thread.sleep()이 2초간 수행되며,
     * 마지막으로 수행되는 업데이트에 의해 앞선 트랜잭션의 업데이트가 사라지는 lost update를 확인할 수 있다.
     *
     * @param seatId 자리 아이디
     * @param userId 사용자 아이디
     * @return 201 CREATED with success message if reserved, 400 if seat already reserved, 404 if seat or user not found.
     */
    @PostMapping("/reserve-unsafe/{seatId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional // to keep the session open
    public ResponseEntity<String> reserveUnsafe(
            @PathVariable Long seatId,
            @RequestParam Long userId) {
                
        //  lock 없이 조회
        var seatOpt = seatRepository.findById(seatId);
        if (seatOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        //  lock 없이 조회
        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Seat seat = seatOpt.get();
        User user = userOpt.get();
        
        if (!seat.isAvailable()) {
            return ResponseEntity.badRequest()
                .body("Seat " + seat.getLabel() + " is already reserved by " + seat.getReservedBy().getEmail());
        }

        // race condition 재현하기 위해 스레드슬립 
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        seat.setReservedBy(user);
        seatRepository.save(seat);

        return ResponseEntity.ok("Seat " + seat.getLabel() + " reserved by " + user.getEmail());
    }


}
