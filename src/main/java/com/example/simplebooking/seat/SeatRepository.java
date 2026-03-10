package com.example.simplebooking.seat;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    /**
     * pessimistic lock 으로 로우를 읽어온다
     * SELECT * FROM ... FOR UPDATE와 동일
     *
     * PESSIMISTIC_READ 는 FOR SHARE 와 동일
     *
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :id")
    Optional<Seat> findByIdForUpdate(@Param("id") Long id);
}
