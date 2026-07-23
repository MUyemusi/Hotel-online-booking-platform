
package com.zhisu.hotel.roomtype.service;

import com.zhisu.hotel.booking.service.BookingDatePolicyService;
import com.zhisu.hotel.booking.service.StayDateRange;
import com.zhisu.hotel.common.enums.StayType;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.inventory.service.InventoryService;
import com.zhisu.hotel.pricing.service.PriceQuoteService;
import com.zhisu.hotel.pricing.vo.PriceQuoteVO;
import com.zhisu.hotel.roomtype.entity.RoomType;
import com.zhisu.hotel.roomtype.entity.RoomTypeImage;
import com.zhisu.hotel.roomtype.mapper.RoomTypeFacilityMapper;
import com.zhisu.hotel.roomtype.mapper.RoomTypeImageMapper;
import com.zhisu.hotel.roomtype.mapper.RoomTypeMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PublicRoomTypeServiceTest {

    private final RoomTypeMapper roomTypes = mock(RoomTypeMapper.class);
    private final RoomTypeImageMapper images = mock(RoomTypeImageMapper.class);
    private final RoomTypeFacilityMapper facilities = mock(RoomTypeFacilityMapper.class);
    private final InventoryService inventory = mock(InventoryService.class);
    private final PriceQuoteService quotes = mock(PriceQuoteService.class);
    private final BookingDatePolicyService datePolicy = mock(BookingDatePolicyService.class);
    private final PublicRoomTypeService service = new PublicRoomTypeService(roomTypes, images, facilities, inventory, quotes, datePolicy);

    @Test
    void rejectsInvalidStayRangeBeforeQueryingData() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        when(datePolicy.resolveStay(tomorrow, tomorrow, "STANDARD"))
                .thenThrow(new BusinessException("INVALID_STAY_DATES", "日期无效"));
        assertThatThrownBy(() -> service.search(tomorrow, tomorrow, 1, 1, "STANDARD"))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("INVALID_STAY_DATES");
    }

    @Test
    void filtersByGuestCapacityAndReturnsMainImageAndDynamicQuote() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(1);
        when(datePolicy.resolveStay(checkIn, checkOut, "STANDARD")).thenReturn(
                new StayDateRange(StayType.STANDARD, checkIn, checkOut, checkIn, checkOut, 1));
        RoomType single = RoomType.builder().id(1L).name("Single").status("ONLINE").maxGuests(1).basePrice(new BigDecimal("100.00")).build();
        RoomType doubleRoom = RoomType.builder().id(2L).name("Double").status("ONLINE").maxGuests(2).basePrice(new BigDecimal("200.00")).build();
        when(roomTypes.selectList(any())).thenReturn(List.of(single, doubleRoom));
        when(images.selectList(any())).thenReturn(List.of(RoomTypeImage.builder().roomTypeId(2L).imageType("MAIN").imageUrl("/main.jpg").sortOrder(1).build()));
        when(facilities.selectList(any())).thenReturn(List.of());
        when(inventory.getMinAvailableCount(2L, checkIn, checkOut.minusDays(1))).thenReturn(3);
        when(quotes.quotePrice(2L, checkIn, checkOut, 1, "STANDARD")).thenReturn(PriceQuoteVO.builder().totalAmount(new BigDecimal("240.00")).dailyPrices(List.of()).build());

        var result = service.search(checkIn, checkOut, 1, 2, "STANDARD");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(0).getMainImageUrl()).isEqualTo("/main.jpg");
        assertThat(result.get(0).getTotalAmount()).isEqualByComparingTo("240.00");
        assertThat(result.get(0).getAvailableRooms()).isEqualTo(3);
    }
}
