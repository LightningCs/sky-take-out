package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/user/shop")
@RestController(value = "UserShopController")
@Api("店铺操作接口")
@Slf4j
public class ShopController {

    private static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/status")
    @ApiOperation("C端获取店铺状态")
    public Result<Integer> getStatus() {

        Integer shopStatus = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("当前店铺状态：{}", shopStatus == 1 ? "营业中" : "休息中");

        return Result.success(shopStatus);
    }
}
