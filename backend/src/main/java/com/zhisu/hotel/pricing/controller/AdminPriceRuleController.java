package com.zhisu.hotel.pricing.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.pricing.dto.PriceRuleSaveRequest;
import com.zhisu.hotel.pricing.entity.PriceRule;
import com.zhisu.hotel.pricing.service.PriceRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/pricing/rules")
@RequiredArgsConstructor
public class AdminPriceRuleController {
    private final PriceRuleService priceRuleService;

    @GetMapping
    public ApiResponse<Page<PriceRule>> page(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(required = false) Long roomTypeId) {
        return ApiResponse.success(priceRuleService.pageRules(page, size, status, roomTypeId));
    }

    @GetMapping("/{id}")
    public ApiResponse<PriceRule> detail(@PathVariable Long id) {
        return ApiResponse.success(priceRuleService.getRule(id));
    }

    @PostMapping
    public ApiResponse<PriceRule> create(@Valid @RequestBody PriceRuleSaveRequest request) {
        return ApiResponse.success(priceRuleService.createRule(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PriceRule> update(@PathVariable Long id, @Valid @RequestBody PriceRuleSaveRequest request) {
        return ApiResponse.success(priceRuleService.updateRule(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<PriceRule> status(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ApiResponse.success(priceRuleService.updateStatus(id, body.get("status")));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        priceRuleService.deleteRule(id);
        return ApiResponse.success();
    }
}
