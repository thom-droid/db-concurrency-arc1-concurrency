package com.example.simplebooking.seat;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    /**
     * 자리를 예약한다.
     *
     * @param seatId 자리 아이디
     * @return 201 CREATED with success message if reserved, 400 if seat already reserved, 404 if seat or user not found.
     */
    @PostMapping("/reserve-unsafe/{seatId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> reserveUnsafe(@PathVariable Long seatId) {
        seatService.reserveUnsafe(seatId);
        return ResponseEntity.ok("created");
    }

}
