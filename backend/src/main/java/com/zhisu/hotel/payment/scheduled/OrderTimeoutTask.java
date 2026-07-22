package com.zhisu.hotel.payment.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhisu.hotel.booking.entity.BookingOrder;
import com.zhisu.hotel.booking.mapper.BookingOrderMapper;
import com.zhisu.hotel.common.enums.CancelType;
import com.zhisu.hotel.common.enums.OrderStatus;
import com.zhisu.hotel.inventory.service.InventoryService;
import com.zhisu.hotel.payment.entity.CancellationRecord;
import com.zhisu.hotel.payment.entity.ScheduledTaskLog;
import com.zhisu.hotel.payment.mapper.CancellationRecordMapper;
import com.zhisu.hotel.payment.mapper.ScheduledTaskLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final BookingOrderMapper bookingOrderMapper;
    private final CancellationRecordMapper cancellationRecordMapper;
    private final ScheduledTaskLogMapper scheduledTaskLogMapper;
    private final InventoryService inventoryService;

    @Scheduled(fixedRate = 60000)
    @Transactional(rollbackFor = Exception.class)
    public void closeTimeoutOrders() {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime now = LocalDateTime.now();
        int closed = 0;
        int released = 0;
        boolean success = true;
        String errorMsg = null;

        try {
            LambdaQueryWrapper<BookingOrder> wrapper = new LambdaQueryWrapper<BookingOrder>()
                    .eq(BookingOrder::getOrderStatus, OrderStatus.PENDING_PAYMENT.name())
                    .lt(BookingOrder::getPaymentTimeout, now);
            List<BookingOrder> timeoutOrders = bookingOrderMapper.selectList(wrapper);
            if (timeoutOrders.isEmpty()) return;

            log.info("发现 {} 个超时订单，开始自动关闭", timeoutOrders.size());
            for (BookingOrder order : timeoutOrders) {
                try {
                    order.setOrderStatus(OrderStatus.CLOSED.name());
                    order.setCancelType(CancelType.SYSTEM_CLOSE.name());
                    order.setCancelReason("支付超时，系统自动关闭");
                    order.setCancelledAt(now);
                    bookingOrderMapper.updateById(order);
                    inventoryService.releaseInventory(order.getId());
                    released += order.getRoomCount() * order.getTotalNights();
                    CancellationRecord record = CancellationRecord.builder()
                            .orderId(order.getId()).userId(order.getUserId())
                            .cancelType(CancelType.SYSTEM_CLOSE.name())
                            .cancelReason("支付超时，系统自动关闭").cancelTime(now)
                            .orderStatusBefore(OrderStatus.PENDING_PAYMENT.name()).build();
                    cancellationRecordMapper.insert(record);
                    closed++;
                    log.info("自动关闭超时订单: {} userId={}", order.getOrderNo(), order.getUserId());
                } catch (Exception e) {
                    log.error("关闭订单 {} 失败: {}", order.getOrderNo(), e.getMessage(), e);
                    success = false;
                    if (errorMsg == null) errorMsg = e.getMessage();
                }
            }
        } catch (Exception e) {
            log.error("超时关单定时任务执行异常", e);
            success = false;
            errorMsg = e.getMessage();
        }

        ScheduledTaskLog taskLog = ScheduledTaskLog.builder()
                .taskName("OrderTimeoutTask").executedAt(startTime)
                .closedOrders(closed).releasedRooms(released)
                .success(success ? 1 : 0).errorMessage(errorMsg).build();
        try {
            scheduledTaskLogMapper.insert(taskLog);
        } catch (Exception logException) {
            log.warn("定时任务日志表尚未初始化，跳过日志落库: {}", logException.getMessage());
        }
        log.info("超时关单任务执行完毕: 关闭{}笔订单, 释放{}间夜库存, 成功={}", closed, released, success);
    }
}
