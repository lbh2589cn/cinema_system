package com.cinema.system.pricing.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PricingRuleUpdateRequest {
    private String ruleName;
    private BigDecimal ruleValue;
    private Integer priority;
    private Boolean enabled;
    private String description;
}
