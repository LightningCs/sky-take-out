package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        //业务异常
        //1.地址簿为空
        AddressBook address = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());

        if (address == null) {
            //抛出业务异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //2.购物车数据为空
        Long id = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(id);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if (list == null || list.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //往订单表插入数据
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, order);

        order.setOrderTime(LocalDateTime.now());
        order.setPayStatus(Orders.UN_PAID);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setPhone(address.getPhone());
        order.setConsignee(address.getConsignee());
        order.setUserId(id);

        orderMapper.insert(order);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        //往订单细节表插入n条数据
        list.forEach(item -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(item, orderDetail);
            orderDetail.setOrderId(order.getId());//设置关联的订单id
            orderDetailList.add(orderDetail);
        });

        //批量插入
        orderDetailMapper.insertBatch(orderDetailList);

        //清空购物车数据
        shoppingCartMapper.clean(id);

        //封装VO返回
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(order.getId())
                .orderTime(order.getOrderTime())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .build();

        return orderSubmitVO;
    }
}
