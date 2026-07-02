package com.cinema.system.seat.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "seat_booking",
       uniqueConstraints = @UniqueConstraint(columnNames = {"showing_id", "seat_id"}))
public class SeatBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "showing_id", nullable = false)
    private Long showingId;

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "locked_by")
    private Long lockedBy;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "booked_by")
    private Long bookedBy;

    @Column(name = "booked_at")
    private LocalDateTime bookedAt;

    @Column(name = "order_id")
    private Long orderId;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;
}
