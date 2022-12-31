package com.xha.gulimall.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xha.gulimall.order.entity.OrderEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:45:50
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

}
