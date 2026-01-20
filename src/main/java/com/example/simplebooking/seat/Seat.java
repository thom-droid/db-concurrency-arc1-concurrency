package com.example.simplebooking.seat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "seat")
@NoArgsConstructor
@Getter
@Setter
public class 
Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean reserved;

    // @Column(nullable = false, unique = true)
    // private String label;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "reserved_by_user_id")
    // private User reservedBy;

    // @Version
    // private long version;

    // public Seat(String label) {
        // this.label = label;
    // }

    // public boolean isAvailable(){
        // return reservedBy == null;
    // }

}
