package com.cinema.system.pricing.controller;

import com.cinema.system.common.response.ApiResponse;
import com.cinema.system.pricing.dto.PricingRuleUpdateRequest;
import com.cinema.system.pricing.entity.PricingRule;
import com.cinema.system.pricing.service.PricingRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing-rules")
@RequiredArgsConstructor
public class PricingRuleController {
    private final PricingRuleService pricingRuleService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<PricingRule>> listRules() {
        return ApiResponse.success(pricingRuleService.listRules());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PricingRule> updateRule(@PathVariable Long id,
                                                @RequestBody PricingRuleUpdateRequest request) {
        return ApiResponse.success(pricingRuleService.updateRule(id, request));
    }
}
