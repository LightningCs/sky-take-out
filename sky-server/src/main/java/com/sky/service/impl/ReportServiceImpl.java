package com.sky.service.impl;

import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
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
        for (long i = 0; !begin.plusDays(i - 1).equals(end); i++) {
            dateList.add(begin.plusDays(i));
        }

        // 营业额统计
        List<Double> turnoverList = orderMapper.getTurnoversBYDates(dateList);

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
        for (long i = 0; !begin.plusDays(i - 1).equals(end); i++) {
            dateList.add(begin.plusDays(i));
        }

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
}
