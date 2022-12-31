package com.xha.gulimall.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xha.gulimall.order.entity.OrderItemEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:45:50
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {

}
