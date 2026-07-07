package com.cinema.system.pricing.repository;

import com.cinema.system.pricing.entity.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    List<PricingRule> findByEnabledTrueOrderByPriorityAsc();
    List<PricingRule> findAllByOrderByPriorityAsc();
}
