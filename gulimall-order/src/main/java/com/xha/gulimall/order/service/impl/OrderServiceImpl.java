package com.xha.gulimall.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.constants.CacheConstants;
import com.xha.gulimall.common.constants.rabbitmq.order.OrderRmConstants;
import com.xha.gulimall.common.to.cart.CartInfoTO;
import com.xha.gulimall.common.to.member.MemberTO;
import com.xha.gulimall.common.to.member.ReceiveAddressTO;
import com.xha.gulimall.common.to.order.OrderItemTO;
import com.xha.gulimall.common.to.order.OrderTO;
import com.xha.gulimall.common.to.product.SpuInfoTO;
import com.xha.gulimall.common.to.seckill.SeskillOrderTO;
import com.xha.gulimall.common.to.ware.WareSkuLockTO;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.order.dao.OrderDao;
import com.xha.gulimall.order.dao.OrderItemDao;
import com.xha.gulimall.order.dto.OrderSubmitDTO;
import com.xha.gulimall.order.entity.OrderEntity;
import com.xha.gulimall.order.entity.OrderItemEntity;
import com.xha.gulimall.order.entity.PaymentInfoEntity;
import com.xha.gulimall.order.enums.OrderStatusEnum;
import com.xha.gulimall.order.feign.CartFeign;
import com.xha.gulimall.order.feign.MemberFeign;
import com.xha.gulimall.order.feign.ProductFeign;
import com.xha.gulimall.order.feign.WareFeign;
import com.xha.gulimall.order.interceptor.LoginInterceptor;
import com.xha.gulimall.order.service.OrderItemService;
import com.xha.gulimall.order.service.OrderService;
import com.xha.gulimall.order.service.PaymentInfoService;
import com.xha.gulimall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
import java.util.Objects;
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

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private OrderDao orderDao;

    @Resource
    private OrderItemDao orderItemDao;

    @Resource
    private PaymentInfoService paymentInfoService;

    @Resource
    private OrderService orderService;

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
     * ????????????????????????????????????
     *
     * @return {@link OrderConfirmVO}
     */
    @Override
    public OrderConfirmVO confirmOrder() throws ExecutionException, InterruptedException {
//        ????????????????????????????????????
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        OrderConfirmVO orderConfirmVO = new OrderConfirmVO();
//        1.??????????????????????????????id
        MemberTO memberTO = LoginInterceptor.threadLoginUser.get();
//        2.?????????????????????????????????????????????id?????????????????????????????????
        CompletableFuture<Void> receiveAddressFuture = CompletableFuture.runAsync(() -> {
//            ???????????????????????????????????????
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<ReceiveAddressTO> receiveAddressList = memberFeign.getReceiveAddressList(memberTO.getId());
            orderConfirmVO.setAddress(receiveAddressList);
        }, threadPoolExecutor);

//        3.????????????????????????????????????????????????????????????
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

//        4.??????????????????
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

//        5.??????????????????
        orderConfirmVO.setIntegration(memberTO.getIntegration());

//        6.??????????????????
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
     * ????????????
     *
     * @param orderSubmitDTO ????????????dto
     * @return {@link SubmitOrderResponseVO}
     */
    @Transactional
    @Override
    public SubmitOrderResponseVO submitOrder(OrderSubmitDTO orderSubmitDTO) {
        threadLocal.set(orderSubmitDTO);
        SubmitOrderResponseVO submitOrderResponseVO = new SubmitOrderResponseVO();
//        1.??????????????????????????????
        MemberTO memberTO = LoginInterceptor.threadLoginUser.get();
        String orderToken = orderSubmitDTO.getOrderToken();
//        2.??????lua??????????????????token???????????????????????????????????????
        String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long executeResult = stringRedisTemplate.execute(
                new DefaultRedisScript<Long>(luaScript, Long.class),
                Arrays.asList(CacheConstants.USER_ORDER_TOKEN_CACHE + memberTO.getId()),
                orderToken);
        if (executeResult == 0L) {
            submitOrderResponseVO.setCode(0);
//            2.1??????????????????
            return submitOrderResponseVO;
        } else {
//            2.2?????????????????????????????????
            CreateOrderVO createOrderVO = createOrder();
//        3.????????????????????????
//            3.1????????????
            OrderEntity order = createOrderVO.getOrder();
            save(order);
//            3.2???????????????
            List<OrderItemEntity> orderItems = createOrderVO.getOrderItems();
            orderItemService.saveBatch(orderItems);
//        4.????????????
//            4.1??????WareSkuLockTO??????
            WareSkuLockTO wareSkuLockTO = new WareSkuLockTO();
            List<OrderItemTO> orderItemTOList = orderItems.stream().map(orderItemEntity -> {
                OrderItemTO orderItemTO = new OrderItemTO();
                orderItemTO.setSkuId(orderItemEntity.getSkuId())
                        .setCount(orderItemEntity.getSkuQuantity());
                return orderItemTO;
            }).collect(Collectors.toList());
            wareSkuLockTO.setOrderSn(order.getOrderSn())
                    .setOrderItemTOS(orderItemTOList);
//            4.2??????????????????,????????????
            R lockResult = wareFeign.wareSkuLock(wareSkuLockTO);
            if (lockResult.getCode() == 0) {
//          5.????????????????????????????????????rabbitmq???????????????
                rabbitTemplate.convertAndSend(
                        OrderRmConstants.ORDER_EVENT_EXCHANGE,
                        OrderRmConstants.ORDER_CREATE_ORDER_BINDING,
                        order);

                submitOrderResponseVO.setOrder(order);
                return submitOrderResponseVO;
            }

        }
        return submitOrderResponseVO;
    }

    /**
     * ????????????
     *
     * @return {@link CreateOrderVO}
     */
    private CreateOrderVO createOrder() {
        CreateOrderVO createOrderVO = new CreateOrderVO();
        OrderEntity orderEntity = new OrderEntity();
//        1.????????????
        orderEntity = buildOrder(orderEntity);

//        2.???????????????
        List<OrderItemEntity> orderItemEntityList = buildOrderItem(orderEntity.getOrderSn());

//        3.?????????????????????????????????????????????????????????
        BigDecimal totalPrice = new BigDecimal(0);
        for (OrderItemEntity orderItemEntity : orderItemEntityList) {
            totalPrice = totalPrice.add(orderItemEntity.getRealAmount());
        }

//        4.??????????????????
        orderEntity.setTotalAmount(totalPrice)
                .setPayAmount(totalPrice)
                .setFreightAmount(BigDecimal.valueOf(0));

        createOrderVO.setOrder(orderEntity)
                .setOrderItems(orderItemEntityList)
                .setPayPrice(totalPrice);
        return createOrderVO;
    }

    /**
     * ????????????
     *
     */
    private OrderEntity buildOrder(OrderEntity orderEntity) {
//        1.????????????
        String orderSn = IdWorker.getTimeId();
        orderEntity.setOrderSn(orderSn);

//          2.1???ThreadLocal??????????????????????????????id
        OrderSubmitDTO orderSubmitDTO = threadLocal.get();
//          2.2????????????Member????????????????????????????????????
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
     * ???????????????
     */
    private List<OrderItemEntity> buildOrderItem(String orderSn) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
//        1.??????????????????????????????????????????????????????????????????
        List<CartInfoTO> userCartItems = cartFeign.getUserCartItems();
        List<OrderItemEntity> orderItemEntityList = null;
        if (!CollectionUtils.isEmpty(userCartItems)) {
            orderItemEntityList = userCartItems.stream().map(userCartItem -> {
//                2.?????????spu??????
                SpuInfoTO spuInfoTO = productFeign.getSpuInfo(userCartItem.getSkuId());
                orderItemEntity.setSpuId(spuInfoTO.getSpuId())
                        .setSpuName(spuInfoTO.getSpuName())
                        .setSpuBrand(spuInfoTO.getSpuBrand());
//                3.?????????sku??????
                orderItemEntity.setSkuId(userCartItem.getSkuId())
                        .setSkuName(userCartItem.getTitle())
                        .setSkuPic(userCartItem.getImage()).setSkuPrice(userCartItem.getPrice())
                        .setSkuAttrsVals(String.join(",", userCartItem.getSkuAttr()))
                        .setSkuQuantity(userCartItem.getCount());
//                4.????????????????????????
                orderItemEntity.setPromotionAmount(BigDecimal.valueOf(0))
                        .setCouponAmount(BigDecimal.valueOf(0))
                        .setIntegrationAmount(BigDecimal.valueOf(0))
                        .setRealAmount(userCartItem.getTotalPrice());
//                5.???????????????
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
        }
        return orderItemEntityList;
    }

    /**
     * ??????id????????????
     *
     * @param orderSn ??????sn
     * @return {@link OrderTO}
     */
    @Override
    public OrderTO getOrderById(String orderSn) {
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getOrderSn, orderSn);
        OrderEntity order = getOne(queryWrapper);
        if (Objects.isNull(order)) {
            return null;
        }
        OrderTO orderTO = new OrderTO();
        BeanUtils.copyProperties(order, orderTO);
        return orderTO;
    }

    /**
     * ????????????
     *
     * @param orderEntity ????????????
     */
    @Override
    public void closeOrder(OrderEntity orderEntity) {
//        1.???????????????????????????
        OrderEntity order = orderDao.selectById(orderEntity.getId());
//        2.???????????????????????????????????????
        if (order.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            System.out.println("??????????????????????????????????????????");
//           2.1????????????
            order.setStatus(OrderStatusEnum.CANCEL.getCode());
            orderDao.updateById(order);
            OrderTO orderTO = new OrderTO();
            BeanUtils.copyProperties(order, orderTO);
            rabbitTemplate.convertAndSend(OrderRmConstants.ORDER_EVENT_EXCHANGE,
                    OrderRmConstants.ORDER_RELEASE_OTHER_BINDING,
                    orderTO);
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param params ????????????
     * @return {@link PageUtils}
     */
    @Override
    public PageUtils getOrderList(Map<String, Object> params) {
//        1.???????????????????????????
        MemberTO memberTO = LoginInterceptor.threadLoginUser.get();
//        2.?????????????????????????????????
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getMemberId, memberTO.getId())
                        .orderByDesc(OrderEntity::getId)
        );
//        3.??????????????????????????????
        List<OrderItemEntity> orderItemList = orderItemDao.selectList(null);

        List<OrderEntity> orderEntityList = page.getRecords().stream().map(order -> {
            order.setOrderItemEntities(getOrderItem(orderItemList, order));
            return order;
        }).collect(Collectors.toList());

        page.setRecords(orderEntityList);

        return new PageUtils(page);
    }

    /**
     * ????????????????????????
     *
     * @param orderItemList ??????????????????
     * @param order         ??????
     * @return {@link List}<{@link OrderItemEntity}>
     */
    public List<OrderItemEntity> getOrderItem(List<OrderItemEntity> orderItemList, OrderEntity order) {
        List<OrderItemEntity> orderItemEntityList = orderItemList.stream()
                .filter(orderItem -> orderItem.getOrderSn().equals(order.getOrderSn()))
                .collect(Collectors.toList());
        return orderItemEntityList;
    }


    /**
     * ???????????????????????????????????????
     *
     * @param aliPayAsyncNotifyVO ??????
     * @return {@link String}
     */
    @Override
    public String handleAliPayAsyncNotifyResponse(AliPayAsyncNotifyVO aliPayAsyncNotifyVO) {
//        1.??????????????????
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setOrderSn(aliPayAsyncNotifyVO.getOut_trade_no())
                .setAlipayTradeNo(aliPayAsyncNotifyVO.getTrade_no())
                .setPaymentStatus(aliPayAsyncNotifyVO.getTrade_status())
                .setCallbackTime(aliPayAsyncNotifyVO.getNotify_time());
        paymentInfoService.save(paymentInfoEntity);
        if (aliPayAsyncNotifyVO.getTrade_status().equals("TRADE_SUCCESS") ||
                aliPayAsyncNotifyVO.getTrade_status().equals("TRADE_FINISHED")) {
//        2.?????????????????????????????????
            LambdaUpdateWrapper<OrderEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(OrderEntity::getOrderSn, aliPayAsyncNotifyVO.getOut_trade_no())
                    .set(OrderEntity::getStatus, OrderStatusEnum.PAYED.getCode());
            orderService.update(updateWrapper);
        }
        return "success";
    }

    /**
     * ??????????????????
     *
     */
    @Override
    public void createSeckillOrder(SeskillOrderTO seckillOrderTO) {
//        1.????????????
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setMemberId(seckillOrderTO.getMemberId())
                .setOrderSn(seckillOrderTO.getOrderSn())
                .setStatus(OrderStatusEnum.CREATE_NEW.getCode())
                .setPayAmount(seckillOrderTO.getSeckillPrice());
        save(orderEntity);
//        2.???????????????
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(seckillOrderTO.getOrderSn())
                .setSkuId(seckillOrderTO.getSkuId())
                .setSkuQuantity(seckillOrderTO.getNum())
                .setSkuPrice(seckillOrderTO.getSeckillPrice());
        orderItemService.save(orderItemEntity);
    }
}
