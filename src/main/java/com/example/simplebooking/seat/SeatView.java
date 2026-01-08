package com.example.simplebooking.seat;

public record SeatView(Long id, String label, Long reservedByUserId) {

    public SeatView(Seat seat) {
        this(seat.getId(), seat.getLabel(), seat.getReservedBy() != null ? seat.getReservedBy().getId() : null);
    }

}
