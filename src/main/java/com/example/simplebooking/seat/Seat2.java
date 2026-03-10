package com.example.simplebooking.seat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "seat_2")
public class Seat2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private boolean reserved;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

}