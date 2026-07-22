package com.zhisu.hotel.pricing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.pricing.dto.PriceRuleSaveRequest;
import com.zhisu.hotel.pricing.entity.PriceRule;

import java.time.LocalDate;

public interface PriceRuleService {
    Page<PriceRule> pageRules(int page, int size, String status, Long roomTypeId);
    PriceRule getRule(Long id);
    PriceRule createRule(PriceRuleSaveRequest request);
    PriceRule updateRule(Long id, PriceRuleSaveRequest request);
    PriceRule updateStatus(Long id, String status);
    void deleteRule(Long id);
    PriceRule resolveRule(LocalDate date, Long roomTypeId);
}
