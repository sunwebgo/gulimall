package com.xha.gulimall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.to.CartInfoTO;
import com.xha.gulimall.common.to.MemberTO;
import com.xha.gulimall.common.to.ReceiveAddressTO;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.order.dao.OrderDao;
import com.xha.gulimall.order.entity.OrderEntity;
import com.xha.gulimall.order.feign.CartFeign;
import com.xha.gulimall.order.feign.MemberFeign;
import com.xha.gulimall.order.interceptor.LoginInterceptor;
import com.xha.gulimall.order.service.OrderService;
import com.xha.gulimall.order.vo.OrderConfirmVO;
import com.xha.gulimall.order.vo.OrderItemVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private MemberFeign memberFeign;

    @Resource
    private CartFeign cartFeign;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 返回确认订单页所需的数据
     *
     * @return {@link OrderConfirmVO}
     */
    @Override
    public OrderConfirmVO confirmOrder() throws ExecutionException, InterruptedException {
//        获取到当前线程的请求数据
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        OrderConfirmVO orderConfirmVO = new OrderConfirmVO();
//        1.获取到当前登录用户的id
        MemberTO memberTO = LoginInterceptor.threadLoginUser.get();
//        2.远程调用会员服务，根据当前用户id查询用户的收货地址列表
        CompletableFuture<Void> receiveAddressFuture = CompletableFuture.runAsync(() -> {
//            新线程共享主线程的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<ReceiveAddressTO> receiveAddressList = memberFeign.getReceiveAddressList(memberTO.getId());
            orderConfirmVO.setAddress(receiveAddressList);
        }, threadPoolExecutor);

//        3.远程调用购物车服务，得到当前的购物项列表
        CompletableFuture<List<OrderItemVO>> orderItemListFuture = CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<CartInfoTO> userCartItems = cartFeign.getUserCartItems();

            List<OrderItemVO> orderItemList = userCartItems.stream().map(userCartItem -> {
                OrderItemVO orderItemVO = new OrderItemVO();
                BeanUtils.copyProperties(userCartItem, orderItemVO);
                return orderItemVO;
            }).collect(Collectors.toList());

            orderConfirmVO.setItems(orderItemList);
            orderConfirmVO.setCount(orderItemList.size());
            return orderItemList;
        }, threadPoolExecutor);

//        4.设置商品总额
        CompletableFuture<Void> skuTotalPriceFuture = orderItemListFuture.thenAcceptAsync((orderItemList) -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<BigDecimal> priceList = orderItemList.stream()
                    .map(orderItem -> {
                        return orderItem.getTotalPrice();
                    }).collect(Collectors.toList());
            BigDecimal totalPrice = new BigDecimal(0);
            for (BigDecimal price : priceList) {
                totalPrice = totalPrice.add(price);
            }
            orderConfirmVO.setTotalPrice(totalPrice);
            orderConfirmVO.setPayPrice(totalPrice);
        }, threadPoolExecutor);

//        5.设置用户积分
        orderConfirmVO.setIntegration(memberTO.getIntegration());

        CompletableFuture.allOf(receiveAddressFuture,
                orderItemListFuture,
                skuTotalPriceFuture).get();
        return orderConfirmVO;
    }

}
