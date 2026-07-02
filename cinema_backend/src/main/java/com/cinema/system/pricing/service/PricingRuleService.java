package com.cinema.system.pricing.service;

import com.cinema.system.common.exception.BusinessException;
import com.cinema.system.pricing.dto.PricingRuleUpdateRequest;
import com.cinema.system.pricing.entity.PricingRule;
import com.cinema.system.pricing.repository.PricingRuleRepository;
import com.cinema.system.seat.repository.SeatBookingRepository;
import com.cinema.system.showing.entity.Showing;
import com.cinema.system.showing.repository.ShowingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingRuleService {
    private final PricingRuleRepository pricingRuleRepository;
    private final ShowingRepository showingRepository;
    private final SeatBookingRepository seatBookingRepository;

    public BigDecimal calculateDiscount(Long showingId, Long userId, BigDecimal totalAmount) {
        List<PricingRule> rules = pricingRuleRepository.findByEnabledTrueOrderByPriorityDesc();
        Showing showing = showingRepository.findById(showingId).orElse(null);

        BigDecimal finalAmount = totalAmount;
        for (PricingRule rule : rules) {
            switch (rule.getRuleType()) {
                case "TUESDAY_DISCOUNT":
                    if (showing != null && showing.getShowDate().getDayOfWeek() == DayOfWeek.TUESDAY) {
                        finalAmount = finalAmount.multiply(rule.getRuleValue());
                    }
                    break;
                case "MEMBER_DISCOUNT":
                    // 会员折扣由订单服务传入 userId 后检查
                    // 简化处理：此处占位，实际应在订单服务中判断
                    break;
                case "PEAK_SURCHARGE":
                    if (showing != null && isPeakTime(showing.getShowTime())) {
                        finalAmount = finalAmount.multiply(rule.getRuleValue());
                    }
                    break;
                default:
                    break;
            }
        }
        return totalAmount.subtract(finalAmount.setScale(2, RoundingMode.HALF_UP));
    }

    private boolean isPeakTime(LocalTime time) {
        return !time.isBefore(LocalTime.of(18, 0)) && !time.isAfter(LocalTime.of(22, 0));
    }

    public List<PricingRule> listRules() {
        return pricingRuleRepository.findByEnabledTrueOrderByPriorityDesc();
    }

    public PricingRule updateRule(Long id, PricingRuleUpdateRequest request) {
        PricingRule rule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("定价规则不存在"));
        if (request.getRuleName() != null) rule.setRuleName(request.getRuleName());
        if (request.getRuleValue() != null) rule.setRuleValue(request.getRuleValue());
        if (request.getPriority() != null) rule.setPriority(request.getPriority());
        if (request.getEnabled() != null) rule.setEnabled(request.getEnabled());
        if (request.getDescription() != null) rule.setDescription(request.getDescription());
        return pricingRuleRepository.save(rule);
    }
}
