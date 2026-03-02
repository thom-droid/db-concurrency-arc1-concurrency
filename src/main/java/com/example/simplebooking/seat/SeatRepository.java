package com.example.simplebooking.seat;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    // Optional<Seat> findByLabel(String label);

    @Modifying
    @Query("""
        UPDATE Seat s
        SET s.reserved = true
        WHERE s.id = :id AND s.reserved = false
    """)
    int reserveIfAvailable(@Param("id") Long id);

}
