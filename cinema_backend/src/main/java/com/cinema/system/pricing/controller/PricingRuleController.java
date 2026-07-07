package com.cinema.system.pricing.controller;

import com.cinema.system.common.response.ApiResponse;
import com.cinema.system.pricing.dto.DiscountResult;
import com.cinema.system.pricing.dto.PricingRuleCreateRequest;
import com.cinema.system.pricing.dto.PricingRuleUpdateRequest;
import com.cinema.system.pricing.entity.PricingRule;
import com.cinema.system.pricing.service.PricingRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PricingRule> createRule(@RequestBody PricingRuleCreateRequest request) {
        return ApiResponse.success(pricingRuleService.createRule(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PricingRule> updateRule(@PathVariable Long id,
                                                @RequestBody PricingRuleUpdateRequest request) {
        return ApiResponse.success(pricingRuleService.updateRule(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteRule(@PathVariable Long id) {
        pricingRuleService.deleteRule(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/calculate")
    public ApiResponse<DiscountResult> calculate(@RequestBody Map<String, Object> params,
                                                  Authentication auth) {
        Long showingId = Long.valueOf(params.get("showingId").toString());
        int ticketCount = Integer.parseInt(params.get("ticketCount").toString());

        Long userId = null;
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            userId = (Long) auth.getPrincipal();
        }

        BigDecimal totalAmount = new BigDecimal(params.get("totalAmount").toString());
        DiscountResult result = pricingRuleService.calculateDiscount(showingId, userId, totalAmount, ticketCount);
        return ApiResponse.success(result);
    }
}
