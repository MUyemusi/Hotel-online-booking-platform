package com.zhisu.hotel.pricing.service.impl;

import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.pricing.dto.PriceRuleSaveRequest;
import com.zhisu.hotel.pricing.entity.PriceRule;
import com.zhisu.hotel.pricing.mapper.HolidayCalendarMapper;
import com.zhisu.hotel.pricing.mapper.PriceRuleMapper;
import com.zhisu.hotel.pricing.service.PriceRuleService;
import com.zhisu.hotel.pricing.vo.DatePriceRule;
import com.zhisu.hotel.systemconfig.service.SystemConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PriceRuleServiceImplTest {

    @Test
    void rejectsEndDateBeforeStartDate() {
        PriceRuleServiceImpl service = new PriceRuleServiceImpl(mock(PriceRuleMapper.class), mock(JdbcTemplate.class), mock(org.springframework.context.ApplicationEventPublisher.class));
        PriceRuleSaveRequest request = new PriceRuleSaveRequest();
        request.setRuleName("测试规则");
        request.setStartDate(LocalDate.of(2026, 10, 2));
        request.setEndDate(LocalDate.of(2026, 10, 1));
        request.setCoefficient(new BigDecimal("1.20"));
        request.setPriority(100);
        request.setStatus("ENABLED");
        BusinessException exception = assertThrows(BusinessException.class, () -> service.createRule(request));
        assertEquals("PRICE_RULE_DATE_INVALID", exception.getCode());
    }

    @Test
    void customPriceRuleTakesPrecedenceOverHolidayCalendar() {
        HolidayCalendarMapper holidayMapper = mock(HolidayCalendarMapper.class);
        SystemConfigService configService = mock(SystemConfigService.class);
        PriceRuleService priceRuleService = mock(PriceRuleService.class);
        PriceRule rule = new PriceRule();
        rule.setRuleName("国庆指定房型活动");
        rule.setCoefficient(new BigDecimal("1.50"));
        when(priceRuleService.resolveRule(LocalDate.of(2026, 10, 2), 5L)).thenReturn(rule);
        HolidayCalendarServiceImpl service = new HolidayCalendarServiceImpl(holidayMapper, configService, priceRuleService);

        DatePriceRule result = service.resolveDateRule(LocalDate.of(2026, 10, 2), 5L);

        assertEquals(new BigDecimal("1.50"), result.coefficient());
        assertEquals("PRICE_RULE", result.priceSource());
        assertEquals("国庆指定房型活动", result.description());
        verifyNoInteractions(holidayMapper, configService);
    }
}
