package com.xha.gulimall.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.constants.CacheConstants;
import com.xha.gulimall.common.to.*;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.order.dao.OrderDao;
import com.xha.gulimall.order.dto.OrderSubmitDTO;
import com.xha.gulimall.order.entity.OrderEntity;
import com.xha.gulimall.order.entity.OrderItemEntity;
import com.xha.gulimall.order.enums.OrderStatusEnum;
import com.xha.gulimall.order.feign.CartFeign;
import com.xha.gulimall.order.feign.MemberFeign;
import com.xha.gulimall.order.feign.ProductFeign;
import com.xha.gulimall.order.feign.WareFeign;
import com.xha.gulimall.order.interceptor.LoginInterceptor;
import com.xha.gulimall.order.service.OrderItemService;
import com.xha.gulimall.order.service.OrderService;
import com.xha.gulimall.order.vo.CreateOrderVO;
import com.xha.gulimall.order.vo.OrderConfirmVO;
import com.xha.gulimall.order.vo.OrderItemVO;
import com.xha.gulimall.order.vo.SubmitOrderResponseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private MemberFeign memberFeign;

    @Resource
    private CartFeign cartFeign;

    @Resource
    private ProductFeign productFeign;

    @Resource
    private OrderItemService orderItemService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private WareFeign wareFeign;

    private ThreadLocal<OrderSubmitDTO> threadLocal = new ThreadLocal<>();

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

//        6.设置防重令牌
        String token = IdUtil.simpleUUID();
        stringRedisTemplate.opsForValue().set(CacheConstants.USER_ORDER_TOKEN_CACHE + memberTO.getId(),
                token,
                30,
                TimeUnit.MINUTES);
        orderConfirmVO.setOrderToken(token);

        CompletableFuture.allOf(receiveAddressFuture,
                orderItemListFuture,
                skuTotalPriceFuture).get();
        return orderConfirmVO;
    }

    /**
     * 提交订单
     *
     * @param orderSubmitDTO 订单提交dto
     * @return {@link SubmitOrderResponseVO}
     */
    @Transactional
    @Override
    public SubmitOrderResponseVO submitOrder(OrderSubmitDTO orderSubmitDTO) {
        threadLocal.set(orderSubmitDTO);
        SubmitOrderResponseVO submitOrderResponseVO = new SubmitOrderResponseVO();
//        1.获取到当前登录的用户
        MemberTO memberTO = LoginInterceptor.threadLoginUser.get();
        String orderToken = orderSubmitDTO.getOrderToken();
//        2.使用lua脚本验证订单token（原子验证令牌和删除令牌）
        String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long executeResult = stringRedisTemplate.execute(
                new DefaultRedisScript<Long>(luaScript, Long.class),
                Arrays.asList(CacheConstants.USER_ORDER_TOKEN_CACHE + memberTO.getId()),
                        orderToken);
        if (executeResult == 0L) {
            submitOrderResponseVO.setCode(0);
//            2.1令牌验证失败
            return submitOrderResponseVO;
        } else {
//            2.2令牌验证成功，创建订单
            CreateOrderVO createOrderVO = createOrder();
//        3.保存订单和订单项
//            3.1保存订单
            OrderEntity order = createOrderVO.getOrder();
            save(order);
//            3.2保存订单项
            List<OrderItemEntity> orderItems = createOrderVO.getOrderItems();
            orderItemService.saveBatch(orderItems);
//        4.锁定库存
//            4.1封装WareSkuLockTO对象
            WareSkuLockTO wareSkuLockTO = new WareSkuLockTO();
            List<OrderItemTO> orderItemTOList = orderItems.stream().map(orderItemEntity -> {
                OrderItemTO orderItemTO = new OrderItemTO();
                orderItemTO.setSkuId(orderItemEntity.getSkuId())
                        .setCount(orderItemEntity.getSkuQuantity());
                return orderItemTO;
            }).collect(Collectors.toList());
            wareSkuLockTO.setOrderSn(order.getOrderSn())
                    .setOrderItemTOS(orderItemTOList);
//            4.2调用库存服务,锁定库存
            R lockResult = wareFeign.wareSkuLock(wareSkuLockTO);
            if (lockResult.getCode() == 0){
//            4.3锁定成功
                submitOrderResponseVO.setOrder(order);
                return submitOrderResponseVO;
            }

        }
        return submitOrderResponseVO;
    }

    /**
     * 创建订单
     *
     * @return {@link CreateOrderVO}
     */
    private CreateOrderVO createOrder() {
        CreateOrderVO createOrderVO = new CreateOrderVO();
        OrderEntity orderEntity = new OrderEntity();
//        1.构建订单
        orderEntity = buildOrder(orderEntity);

//        2.设置订单项
        List<OrderItemEntity> orderItemEntityList = buildOrderItem(orderEntity.getOrderSn());

//        3.遍历订单项，累加订单项价格获取到总价格
        BigDecimal totalPrice = new BigDecimal(0);
        for (OrderItemEntity orderItemEntity : orderItemEntityList) {
            totalPrice = totalPrice.add(orderItemEntity.getRealAmount());
        }

//        4.设置订单价格
        orderEntity.setTotalAmount(totalPrice)
                .setPayAmount(totalPrice)
                .setFreightAmount(BigDecimal.valueOf(0));

        createOrderVO.setOrder(orderEntity)
                .setOrderItems(orderItemEntityList)
                .setPayPrice(totalPrice);
        return createOrderVO;
    }

    /**
     * 构建订单
     *
     * @return {@link OrderEntity}
     */
    private OrderEntity buildOrder(OrderEntity orderEntity) {
//        1.构建订单
        String orderSn = IdUtil.simpleUUID();
        orderEntity.setOrderSn(orderSn);

//          2.1从ThreadLocal中获取到当前的收货地id
        OrderSubmitDTO orderSubmitDTO = threadLocal.get();
//          2.2远程调用Member，查询用户的收货地址信息
        ReceiveAddressTO receiveAddress = memberFeign.getReceiveAddress(orderSubmitDTO.getAddrId());
        orderEntity.setMemberId(receiveAddress.getMemberId())
                .setMemberUsername(receiveAddress.getName())
                .setReceiverPhone(receiveAddress.getPhone())
                .setReceiverProvince(receiveAddress.getProvince())
                .setReceiverCity(receiveAddress.getCity())
                .setReceiverRegion(receiveAddress.getRegion())
                .setReceiverDetailAddress(receiveAddress.getDetailAddress())
                .setFreightAmount(BigDecimal.valueOf(0))
                .setStatus(OrderStatusEnum.CREATE_NEW.getCode())
                .setAutoConfirmDay(7);
        return orderEntity;
    }

    /**
     * 构建订单项
     */
    private List<OrderItemEntity> buildOrderItem(String orderSn) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
//        1.远程调用购物车服务，获取当前用户的购物项信息
        List<CartInfoTO> userCartItems = cartFeign.getUserCartItems();
        List<OrderItemEntity> orderItemEntityList = null;
        if (!CollectionUtils.isEmpty(userCartItems)) {
            orderItemEntityList = userCartItems.stream().map(userCartItem -> {
//                2.商品的spu信息
                SpuInfoTO spuInfoTO = productFeign.getSpuInfo(userCartItem.getSkuId());
                orderItemEntity.setSpuId(spuInfoTO.getSpuId())
                        .setSpuName(spuInfoTO.getSpuName())
                        .setSpuBrand(spuInfoTO.getSpuBrand());
//                3.商品的sku信息
                orderItemEntity.setSkuId(userCartItem.getSkuId())
                        .setSkuName(userCartItem.getTitle())
                        .setSkuPic(userCartItem.getImage()).setSkuPrice(userCartItem.getPrice())
                        .setSkuAttrsVals(String.join(",", userCartItem.getSkuAttr()))
                        .setSkuQuantity(userCartItem.getCount());
//                4.订单项的价格信息
                orderItemEntity.setPromotionAmount(BigDecimal.valueOf(0))
                        .setCouponAmount(BigDecimal.valueOf(0))
                        .setIntegrationAmount(BigDecimal.valueOf(0))
                        .setRealAmount(userCartItem.getTotalPrice());
//                5.添加订单号
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
        }
        return orderItemEntityList;
    }


}
