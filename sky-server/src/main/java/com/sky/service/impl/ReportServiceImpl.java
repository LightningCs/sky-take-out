package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService{

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 统计营业额
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();

        // 日期计算
        compute(begin, end, dateList);

        // 营业额统计
        List<Double> turnoverList = orderMapper.getTurnoversByDates(dateList);

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ','))
                .turnoverList(StringUtils.join(turnoverList, ','))
                .build();
    }

    /**
     * 用户统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();

        //日期计算
        compute(begin, end, dateList);

        List<LocalDateTime> dateTimeList = dateList.stream()
                .map(date -> LocalDateTime.of(date, LocalTime.MAX))
                .collect(Collectors.toList());

        //用户统计
        List<Integer> userList = userMapper.getTotalUserList(dateTimeList);

        //新增用户统计
        List<Integer> newUserList = userMapper.getNewUserList(dateList);

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ','))
                .totalUserList(StringUtils.join(userList, ','))
                .newUserList(StringUtils.join(newUserList, ','))
                .build();
    }


    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();

        //日期计算
        compute(begin, end, dateList);

        //每日订单总数 select * from orders where Date(order_time) = date
        List<Integer> orderCountList = orderMapper.getOrderEverydayCount(dateList);

        //每日有效订单总数 select * from orders where status = 5 and Date(order_time) = date
        List<Integer> validOrderCountList = orderMapper.getOrderEverydayCount(dateList, Orders.COMPLETED);

        Integer totalOrderCount = getAll(orderCountList);

        Integer validOrderCount = getAll(validOrderCountList);

        //订单完成率
        Double orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ','))
                .orderCountList(StringUtils.join(orderCountList, ','))
                .validOrderCountList(StringUtils.join(validOrderCountList, ','))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 订单前十统计
     *
     * @return
     */
    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        List<GoodsSalesDTO> top10 = orderMapper.getTop10Goods(begin, end);

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(top10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList()), ','))
                .numberList(StringUtils.join(top10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList()), ','))
                .build();
    }

    private Integer getAll(List<Integer> list) {
        Integer sum = 0;

        for (Integer l : list) sum += l;

        return sum;
    }

    // 日期计算
    private void compute(LocalDate begin, LocalDate end, List<LocalDate> dateList) {
        for (long i = 0; !begin.plusDays(i - 1).equals(end); i++) {
            dateList.add(begin.plusDays(i));
        }
    }
}
