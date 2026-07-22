package com.zhisu.hotel.pricing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.pricing.dto.PriceRuleSaveRequest;
import com.zhisu.hotel.pricing.entity.PriceRule;
import com.zhisu.hotel.pricing.event.PriceRuleChangedEvent;
import com.zhisu.hotel.pricing.mapper.PriceRuleMapper;
import com.zhisu.hotel.pricing.service.PriceRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PriceRuleServiceImpl implements PriceRuleService {
    private final PriceRuleMapper priceRuleMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Page<PriceRule> pageRules(int page, int size, String status, Long roomTypeId) {
        LambdaQueryWrapper<PriceRule> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) wrapper.eq(PriceRule::getStatus, normalizeStatus(status));
        if (roomTypeId != null) wrapper.eq(PriceRule::getRoomTypeId, roomTypeId);
        wrapper.orderByDesc(PriceRule::getPriority).orderByDesc(PriceRule::getStartDate).orderByDesc(PriceRule::getId);
        return priceRuleMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public PriceRule getRule(Long id) {
        PriceRule rule = priceRuleMapper.selectById(id);
        if (rule == null) throw new BusinessException("PRICE_RULE_NOT_FOUND", "价格规则不存在");
        return rule;
    }

    @Override
    @Transactional
    public PriceRule createRule(PriceRuleSaveRequest request) {
        validate(request);
        PriceRule rule = fill(new PriceRule(), request);
        priceRuleMapper.insert(rule);
        publish(rule);
        return getRule(rule.getId());
    }

    @Override
    @Transactional
    public PriceRule updateRule(Long id, PriceRuleSaveRequest request) {
        validate(request);
        PriceRule previous = getRule(id);
        eventPublisher.publishEvent(new PriceRuleChangedEvent(previous.getRoomTypeId(), previous.getStartDate(), previous.getEndDate()));
        PriceRule rule = fill(previous, request);
        priceRuleMapper.updateById(rule);
        publish(rule);
        return getRule(id);
    }

    @Override
    @Transactional
    public PriceRule updateStatus(Long id, String status) {
        PriceRule rule = getRule(id);
        rule.setStatus(normalizeStatus(status));
        priceRuleMapper.updateById(rule);
        publish(rule);
        return getRule(id);
    }

    @Override
    @Transactional
    public void deleteRule(Long id) {
        PriceRule rule = getRule(id);
        priceRuleMapper.deleteById(id);
        publish(rule);
    }

    @Override
    public PriceRule resolveRule(LocalDate date, Long roomTypeId) {
        Integer tableCount = jdbcTemplate.query(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='price_rule'",
                rs -> rs.next() ? rs.getInt(1) : 0);
        if (tableCount == null || tableCount == 0) return null;
        LambdaQueryWrapper<PriceRule> wrapper = new LambdaQueryWrapper<PriceRule>()
                .eq(PriceRule::getStatus, "ENABLED")
                .le(PriceRule::getStartDate, date)
                .ge(PriceRule::getEndDate, date);
        if (roomTypeId == null) {
            wrapper.isNull(PriceRule::getRoomTypeId);
        } else {
            wrapper.and(w -> w.isNull(PriceRule::getRoomTypeId).or().eq(PriceRule::getRoomTypeId, roomTypeId));
        }
        wrapper.last("ORDER BY (room_type_id IS NOT NULL) DESC, priority DESC, id DESC LIMIT 1");
        return priceRuleMapper.selectOne(wrapper);
    }

    private void publish(PriceRule rule) {
        eventPublisher.publishEvent(new PriceRuleChangedEvent(rule.getRoomTypeId(), rule.getStartDate(), rule.getEndDate()));
    }

    private void validate(PriceRuleSaveRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("PRICE_RULE_DATE_INVALID", "结束日期不能早于开始日期");
        }
        normalizeStatus(request.getStatus());
    }

    private PriceRule fill(PriceRule rule, PriceRuleSaveRequest request) {
        rule.setRuleName(request.getRuleName().trim());
        rule.setStartDate(request.getStartDate());
        rule.setEndDate(request.getEndDate());
        rule.setRoomTypeId(request.getRoomTypeId());
        rule.setCoefficient(request.getCoefficient().setScale(2, RoundingMode.HALF_UP));
        rule.setPriority(request.getPriority());
        rule.setStatus(normalizeStatus(request.getStatus()));
        rule.setRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null);
        return rule;
    }

    private String normalizeStatus(String status) {
        String value = status == null ? "" : status.trim().toUpperCase();
        if (!"ENABLED".equals(value) && !"DISABLED".equals(value)) {
            throw new BusinessException("PRICE_RULE_STATUS_INVALID", "价格规则状态必须为 ENABLED 或 DISABLED");
        }
        return value;
    }
}