package com.sky.task;

import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类，定时处理任务订单
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单
     */
//    @Scheduled(cron = "0/5 * * * * ?")
    @Scheduled(cron = "0 * * * * ?")//每分钟触发一次
    public void processTimeOutOrder() {
        log.info("定时处理超时订单，{}", LocalDateTime.now());

        //select * from orders where status = ? and order_time < （当前时间）
        List<Orders> list = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(15));

        if (list != null && list.size() > 0) {
            for (Orders orders : list) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());

                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理一直处于配送中的订单
     */
//    @Scheduled(cron = "0/5 * * * * ?")
    @Scheduled(cron = "0 0 1 * * ?")//每天凌晨一点
    public void processDeliveryOrder() {
        log.info("处理一直处于配送中的订单，{}", LocalDateTime.now());

        List<Orders> list = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().minusHours(1));

        if (list != null && list.size() > 0) {
            for (Orders orders : list) {
                orders.setStatus(Orders.COMPLETED);

                orderMapper.update(orders);
            }
        }
    }
}
