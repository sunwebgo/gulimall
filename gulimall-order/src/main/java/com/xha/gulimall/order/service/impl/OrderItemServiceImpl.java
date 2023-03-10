package com.xha.gulimall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.order.dao.OrderItemDao;
import com.xha.gulimall.order.entity.OrderItemEntity;
import com.xha.gulimall.order.service.OrderItemService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;


@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Resource
    private OrderItemDao orderItemDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 得到订单条目id
     *
     * @param orderSn 订单sn
     * @return {@link OrderItemEntity}
     */
    @Override
    public OrderItemEntity getOrderItemById(String orderSn) {
        LambdaQueryWrapper<OrderItemEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItemEntity::getOrderSn, orderSn);
        OrderItemEntity orderItemEntity = orderItemDao.selectOne(queryWrapper);
        return orderItemEntity;
    }

}
