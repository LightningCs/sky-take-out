package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     * @param order
     */
    void insert(Orders order);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 订单搜索
     * @param pageQueryDTO
     * @return
     */
    Page<Orders> searchOrders(OrdersPageQueryDTO pageQueryDTO);

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @Select("select count(if(status=2,1,null)) as toBeConfirmed, count(if(status=3,1,null)) as confirmed, count(if(status=4,1,null)) as deliveryInProgress from orders")
    OrderStatisticsVO statistics();

    /**
     * 通过订单id获取订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 查询订单历史
     * @param status
     * @return
     */
    Page<OrderVO> getByStatus(Integer status);

    /**
     * 根据订单状态和下单时间查找订单数据
     * @param status
     * @param time
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> getByStatusAndOrderTime(Integer status, LocalDateTime time);
}
