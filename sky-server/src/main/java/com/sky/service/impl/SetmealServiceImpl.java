package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        //套餐中包含未起售菜品
        for (SetmealDish setmealDish : setmealDishes) {
            Dish dish = dishMapper.getById(setmealDish.getDishId());
            if (dish.getStatus() == 0) throw new RuntimeException(MessageConstant.SETMEAL_ENABLE_FAILED);
        }

        //新增套餐
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.save(setmeal);

        //新增套餐菜品关系表
        Long setmealId = setmeal.getId();
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealId));

        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {

        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        Page<Setmeal> setmeal = setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(setmeal.getTotal(), setmeal.getResult());
    }

    /**
     * 套餐起售、停售
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        //判断套餐是否有停售菜品
        List<Dish> dishes = setmealDishMapper.getDishesBySetmealId(id);

        for (Dish dish : dishes) {
            if (dish.getStatus() == 0) {
                throw new RuntimeException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }

        //修改起售停售状态
        Setmeal setmeal = new Setmeal().builder()
                        .id(id)
                        .status(status).build();
        setmealMapper.update(setmeal);
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        //修改套餐表
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        setmealMapper.update(setmeal);

        //修改套餐菜品关系表
        setmealDishMapper.deleteById(setmeal.getId());

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmeal.getId()));

        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 根据id获取套餐数据
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {
        SetmealVO setmealVO = setmealMapper.getById(id);
        List<SetmealDish> dishes = setmealDishMapper.getSetmealDishById(setmealVO.getId());
        setmealVO.setSetmealDishes(dishes);

        return setmealVO;
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //判断套餐是否处于起售状态
        List<Setmeal> setmeals = setmealMapper.getByIds(ids);

        for (Setmeal setmeal : setmeals) {
            if (setmeal.getStatus() == 1) {
                throw new RuntimeException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        //批量删除套餐数据
        setmealMapper.deleteBatch(ids);

        //批量删除套餐菜品关系数据
        setmealDishMapper.deleteBatch(ids);
    }

    /**
     * 通过分类id获取套餐
     * @param setmeal
     * @return
     */
    @Override
    public List<Setmeal> getByCategoryId(Setmeal setmeal) {

        return setmealMapper.list(setmeal);
    }

    /**
     * 根据套餐id查询包含的菜品
     * @param setmealId
     * @return
     */
    @Override
    public List<DishItemVO> getDishItemById(Long setmealId) {
        return setmealDishMapper.getDishesItemsBySetmealId(setmealId);
    }
}
