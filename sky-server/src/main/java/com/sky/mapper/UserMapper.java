package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 通过openid获取用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 新增用户
     * @param user
     */
    void insert(User user);

    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    /**
     * 统计用户总数
     * @param dateTimeList
     * @return
     */
    List<Integer> getTotalUserList(List<LocalDateTime> dateTimeList);

    /**
     * 统计新增用户数量
     * @param dateList
     * @return
     */
    List<Integer> getNewUserList(List<LocalDate> dateList);

    Integer countByMap(Map map);
}
