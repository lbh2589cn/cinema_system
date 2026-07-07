package com.cinema.system.pricing.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class PricingRuleCreateRequest {
    private String ruleName;
    private BigDecimal ruleValue;
    private Boolean enabled;
    private String description;

    private Boolean conditionMember;
    private String conditionWeekdays;
    private LocalTime conditionTimeStart;
    private LocalTime conditionTimeEnd;
    private Integer conditionTicketMin;
    private Integer conditionTicketMax;
    private BigDecimal conditionSeatRatioMin;
    private BigDecimal conditionSeatRatioMax;
}
