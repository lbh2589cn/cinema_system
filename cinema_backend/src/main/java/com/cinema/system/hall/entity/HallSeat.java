package com.cinema.system.hall.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "hall_seat",
       uniqueConstraints = @UniqueConstraint(columnNames = {"hall_id", "row_num", "col_num"}))
public class HallSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hall_id", nullable = false)
    private Long hallId;

    @Column(name = "row_num", nullable = false)
    private Integer rowNum;

    @Column(name = "col_num", nullable = false)
    private Integer colNum;

    @Column(name = "status", nullable = false, length = 20)
    private String status;
}
