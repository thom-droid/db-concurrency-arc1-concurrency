package com.example.simplebooking.seat;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    Optional<Seat> findByLabel(String label);
}
