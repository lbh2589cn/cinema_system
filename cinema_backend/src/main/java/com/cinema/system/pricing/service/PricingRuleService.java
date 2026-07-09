package com.cinema.system.pricing.service;

import com.cinema.system.common.exception.BusinessException;
import com.cinema.system.pricing.dto.DiscountResult;
import com.cinema.system.pricing.dto.PricingRuleCreateRequest;
import com.cinema.system.pricing.dto.PricingRuleUpdateRequest;
import com.cinema.system.pricing.entity.PricingRule;
import com.cinema.system.pricing.repository.PricingRuleRepository;
import com.cinema.system.hall.entity.Hall;
import com.cinema.system.hall.repository.HallRepository;
import com.cinema.system.seat.repository.SeatBookingRepository;
import com.cinema.system.showing.entity.Showing;
import com.cinema.system.showing.repository.ShowingRepository;
import com.cinema.system.user.entity.User;
import com.cinema.system.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingRuleService {
    private final PricingRuleRepository pricingRuleRepository;
    private final ShowingRepository showingRepository;
    private final SeatBookingRepository seatBookingRepository;
    private final UserRepository userRepository;
    private final HallRepository hallRepository;

    public DiscountResult calculateDiscount(Long showingId, Long userId, BigDecimal totalAmount, int ticketCount) {
        List<PricingRule> rules = pricingRuleRepository.findByEnabledTrueOrderByPriorityAsc();
        Showing showing = showingRepository.findById(showingId).orElse(null);
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        BigDecimal currentAmount = totalAmount;
        List<DiscountResult.DiscountItem> items = new ArrayList<>();
        for (PricingRule rule : rules) {
            if (matchesConditions(rule, showing, user, ticketCount)) {
                BigDecimal before = currentAmount;
                currentAmount = currentAmount.multiply(rule.getRuleValue());
                BigDecimal impact = currentAmount.subtract(before).setScale(2, RoundingMode.HALF_UP);
                items.add(new DiscountResult.DiscountItem(rule.getRuleName(), rule.getDescription(), impact));
            }
        }
        BigDecimal finalAmount = currentAmount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalDiscount = totalAmount.subtract(finalAmount);
        return new DiscountResult(totalAmount, finalAmount, totalDiscount, items);
    }

    private boolean matchesConditions(PricingRule rule, Showing showing, User user, int ticketCount) {
        if (rule.getConditionMember() != null) {
            boolean isMember = user != null && Boolean.TRUE.equals(user.getIsMember());
            if (rule.getConditionMember() != isMember) {
                return false;
            }
        }

        if (rule.getConditionWeekdays() != null && !rule.getConditionWeekdays().isEmpty() && showing != null) {
            DayOfWeek dayOfWeek = showing.getShowDate().getDayOfWeek();
            String dayStr = dayOfWeek.name();
            List<String> weekdays = Arrays.asList(rule.getConditionWeekdays().split(","));
            if (!weekdays.contains(dayStr)) {
                return false;
            }
        }

        if (showing != null) {
            LocalTime showTime = showing.getShowTime();
            if (rule.getConditionTimeStart() != null && showTime.isBefore(rule.getConditionTimeStart())) {
                return false;
            }
            if (rule.getConditionTimeEnd() != null && showTime.isAfter(rule.getConditionTimeEnd())) {
                return false;
            }
        }

        if (rule.getConditionTicketMin() != null && ticketCount < rule.getConditionTicketMin()) {
            return false;
        }
        if (rule.getConditionTicketMax() != null && ticketCount > rule.getConditionTicketMax()) {
            return false;
        }

        if (rule.getConditionSeatRatioMin() != null || rule.getConditionSeatRatioMax() != null) {
            Long bookedCount = seatBookingRepository.countBookedByShowingId(showing.getId());
            Hall hall = hallRepository.findById(showing.getHallId()).orElse(null);
            if (hall == null) {
                return false;
            }
            BigDecimal bookedRatio = BigDecimal.valueOf(bookedCount).divide(
                    BigDecimal.valueOf(hall.getSeatCount()), 4, RoundingMode.HALF_UP);
            BigDecimal remainingRatio = BigDecimal.ONE.subtract(bookedRatio);

            if (rule.getConditionSeatRatioMin() != null && remainingRatio.compareTo(rule.getConditionSeatRatioMin()) < 0) {
                return false;
            }
            if (rule.getConditionSeatRatioMax() != null && remainingRatio.compareTo(rule.getConditionSeatRatioMax()) > 0) {
                return false;
            }
        }

        return true;
    }

    public List<PricingRule> listRules() {
        return pricingRuleRepository.findByEnabledTrueOrderByPriorityAsc();
    }

    public PricingRule updateRule(Long id, PricingRuleUpdateRequest request) {
        PricingRule rule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Pricing rule not found"));
        if (request.getRuleName() != null) rule.setRuleName(request.getRuleName());
        if (request.getRuleValue() != null) rule.setRuleValue(request.getRuleValue());
        if (request.getPriority() != null) rule.setPriority(request.getPriority());
        if (request.getEnabled() != null) rule.setEnabled(request.getEnabled());
        if (request.getDescription() != null) rule.setDescription(request.getDescription());
        if (request.getConditionMember() != null) rule.setConditionMember(request.getConditionMember());
        if (request.getConditionWeekdays() != null) rule.setConditionWeekdays(request.getConditionWeekdays());
        if (request.getConditionTimeStart() != null) rule.setConditionTimeStart(request.getConditionTimeStart());
        if (request.getConditionTimeEnd() != null) rule.setConditionTimeEnd(request.getConditionTimeEnd());
        if (request.getConditionTicketMin() != null) rule.setConditionTicketMin(request.getConditionTicketMin());
        if (request.getConditionTicketMax() != null) rule.setConditionTicketMax(request.getConditionTicketMax());
        if (request.getConditionSeatRatioMin() != null) rule.setConditionSeatRatioMin(request.getConditionSeatRatioMin());
        if (request.getConditionSeatRatioMax() != null) rule.setConditionSeatRatioMax(request.getConditionSeatRatioMax());
        return pricingRuleRepository.save(rule);
    }

    public PricingRule createRule(PricingRuleCreateRequest request) {
        PricingRule rule = new PricingRule();
        rule.setRuleName(request.getRuleName());
        rule.setRuleValue(request.getRuleValue());
        rule.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        rule.setDescription(request.getDescription());
        rule.setConditionMember(request.getConditionMember());
        rule.setConditionWeekdays(request.getConditionWeekdays());
        rule.setConditionTimeStart(request.getConditionTimeStart());
        rule.setConditionTimeEnd(request.getConditionTimeEnd());
        rule.setConditionTicketMin(request.getConditionTicketMin());
        rule.setConditionTicketMax(request.getConditionTicketMax());
        rule.setConditionSeatRatioMin(request.getConditionSeatRatioMin());
        rule.setConditionSeatRatioMax(request.getConditionSeatRatioMax());

        List<PricingRule> allRules = pricingRuleRepository.findAllByOrderByPriorityAsc();
        int maxPriority = allRules.isEmpty() ? 0 : allRules.get(allRules.size() - 1).getPriority();
        rule.setPriority(maxPriority + 1);

        return pricingRuleRepository.save(rule);
    }

    public void deleteRule(Long id) {
        pricingRuleRepository.deleteById(id);
        // Reorder priorities for remaining rules
        List<PricingRule> remaining = pricingRuleRepository.findAllByOrderByPriorityAsc();
        int newPriority = 1;
        for (PricingRule rule : remaining) {
            if (rule.getPriority() != newPriority) {
                rule.setPriority(newPriority);
                pricingRuleRepository.save(rule);
            }
            newPriority++;
        }
    }
}
