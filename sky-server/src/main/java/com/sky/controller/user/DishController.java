package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController(value = "UserDishController")
@RequestMapping("/user/dish")
@Api(tags = "C端菜品浏览接口")
@Slf4j
public class DishController {

    @Autowired
    RedisTemplate redisTemplate = new RedisTemplate();

    @Autowired
    private DishService dishService;

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("list")
    @ApiOperation("C端根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        log.info("根据分类id查询菜品：{}", categoryId);

        //构造redis中的key
        String key = "dish_" + categoryId;

        //查询redis中是否存在菜品数据
        List<DishVO> dishVOList = (List<DishVO>) redisTemplate.opsForValue().get(key);

        //如果存在则直接返回，无须查询数据库
        if (dishVOList != null && dishVOList.size() > 0) return Result.success(dishVOList);

        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

        //如果不存在，则查询数据库，并放入redis中
        List<DishVO> dishes = dishService.getDishAndFlavorByCategoryId(dish);
        redisTemplate.opsForValue().set(key, dishes);

        return Result.success(dishes);
    }
}
