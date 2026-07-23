package com.zhisu.hotel.booking.service.impl;

import com.zhisu.hotel.booking.service.StayDateRange;
import com.zhisu.hotel.common.enums.StayType;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.systemconfig.service.SystemConfigService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookingDatePolicyServiceImplTest {
    private static final ZoneId HOTEL_ZONE = ZoneId.of("Asia/Shanghai");

    @Test
    void resolvesOrdinaryOneNightAcrossYearBoundary() {
        SystemConfigService configs = configs();
        Clock clock = Clock.fixed(Instant.parse("2026-12-30T16:00:00Z"), HOTEL_ZONE);
        BookingDatePolicyServiceImpl service = new BookingDatePolicyServiceImpl(configs, clock);

        StayDateRange stay = service.resolveStay(LocalDate.of(2026, 12, 31), LocalDate.of(2027, 1, 1), "STANDARD");

        assertThat(stay.stayNights()).isEqualTo(1);
        assertThat(stay.inventoryStartDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(stay.lastInventoryDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void earlyMorningUsesPreviousBusinessDate() {
        SystemConfigService configs = configs();
        Clock clock = Clock.fixed(Instant.parse("2026-07-22T18:00:00Z"), HOTEL_ZONE);
        BookingDatePolicyServiceImpl service = new BookingDatePolicyServiceImpl(configs, clock);
        LocalDate today = LocalDate.of(2026, 7, 23);

        StayDateRange stay = service.resolveStay(today, today, "EARLY_MORNING");

        assertThat(stay.stayType()).isEqualTo(StayType.EARLY_MORNING);
        assertThat(stay.stayNights()).isEqualTo(1);
        assertThat(stay.inventoryStartDate()).isEqualTo(LocalDate.of(2026, 7, 22));
        assertThat(stay.inventoryEndExclusive()).isEqualTo(today);
    }

    @Test
    void rejectsEarlyMorningAtCutoff() {
        SystemConfigService configs = configs();
        Clock clock = Clock.fixed(Instant.parse("2026-07-22T22:00:00Z"), HOTEL_ZONE);
        BookingDatePolicyServiceImpl service = new BookingDatePolicyServiceImpl(configs, clock);
        LocalDate today = LocalDate.of(2026, 7, 23);

        assertThatThrownBy(() -> service.resolveStay(today, today, "EARLY_MORNING"))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("EARLY_MORNING_WINDOW_CLOSED");
    }

    private SystemConfigService configs() {
        SystemConfigService configs = mock(SystemConfigService.class);
        when(configs.getInteger("BOOKING_WINDOW_MONTHS")).thenReturn(12);
        when(configs.getInteger("MAX_STAY_NIGHTS")).thenReturn(30);
        when(configs.getTime("EARLY_MORNING_END_TIME")).thenReturn(LocalTime.of(6, 0));
        return configs;
    }
}
