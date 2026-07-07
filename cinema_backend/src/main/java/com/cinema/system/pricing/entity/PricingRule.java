package com.cinema.system.pricing.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "pricing_rule")
public class PricingRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Column(name = "rule_value", nullable = false, precision = 10, scale = 4)
    private BigDecimal ruleValue;

    @Column(nullable = false)
    private Integer priority;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(length = 255)
    private String description;

    @Column(name = "condition_member")
    private Boolean conditionMember;

    @Column(name = "condition_weekdays", length = 50)
    private String conditionWeekdays;

    @Column(name = "condition_time_start")
    private LocalTime conditionTimeStart;

    @Column(name = "condition_time_end")
    private LocalTime conditionTimeEnd;

    @Column(name = "condition_ticket_min")
    private Integer conditionTicketMin;

    @Column(name = "condition_ticket_max")
    private Integer conditionTicketMax;

    @Column(name = "condition_seat_ratio_min", precision = 5, scale = 2)
    private BigDecimal conditionSeatRatioMin;

    @Column(name = "condition_seat_ratio_max", precision = 5, scale = 2)
    private BigDecimal conditionSeatRatioMax;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
