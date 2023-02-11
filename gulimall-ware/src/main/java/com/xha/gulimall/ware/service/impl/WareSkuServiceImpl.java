package com.xha.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.constants.NumberConstants;
import com.xha.gulimall.common.to.order.OrderTO;
import com.xha.gulimall.common.to.product.SkuStockTO;
import com.xha.gulimall.common.to.rabbitmq.StockDetailTO;
import com.xha.gulimall.common.to.rabbitmq.StockLockedTO;
import com.xha.gulimall.common.to.ware.WareSkuLockTO;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.common.constants.rabbitmq.ware.WareRmConstants;
import com.xha.gulimall.ware.dao.WareOrderTaskDetailDao;
import com.xha.gulimall.ware.dao.WareSkuDao;
import com.xha.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.xha.gulimall.ware.entity.WareOrderTaskEntity;
import com.xha.gulimall.ware.entity.WareSkuEntity;
import com.xha.gulimall.ware.exption.UnEnoughStockException;
import com.xha.gulimall.ware.feign.OrderFeignService;
import com.xha.gulimall.ware.feign.ProductFeignService;
import com.xha.gulimall.ware.pojo.SkuWareIdList;
import com.xha.gulimall.ware.service.WareOrderTaskDetailService;
import com.xha.gulimall.ware.service.WareOrderTaskService;
import com.xha.gulimall.ware.service.WareSkuService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    private WareSkuDao wareSkuDao;

    @Resource
    private ProductFeignService productFeignService;

    @Resource
    private WareSkuService wareSkuService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private WareOrderTaskService wareOrderTaskService;

    @Resource
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Resource
    private OrderFeignService orderFeignService;

    @Resource
    private WareOrderTaskDetailDao wareOrderTaskDetailDao;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据检索条件查询库存中的sku信息
     *
     * @param params 参数个数
     * @return {@link R}
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        LambdaQueryWrapper<WareSkuEntity> queryWrapper = new LambdaQueryWrapper<>();

//        1.获取到检索关键字
        String skuId = (String) params.get("skuId");
        String wareId = (String) params.get("wareId");

        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq(WareSkuEntity::getSkuId, skuId);
        }
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq(WareSkuEntity::getWareId, wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 添加库存
     *
     * @param skuId  sku id
     * @param wareId 器皿id
     * @param skuNum sku num
     */
    @Override
    public R addStock(Long skuId, Long wareId, Integer skuNum) {
//        1.根据skuID查询sku信息
        String skuName = productFeignService.getSkuName(skuId);
        if (skuName.equals("当前sku信息不存在")) {
            return R.error().put("msg", "当前sku信息不存在");
        }

//        2.封装WareSkuEntity对象
        WareSkuEntity wareSkuEntity = new WareSkuEntity();
        wareSkuEntity
                .setSkuId(skuId)
                .setWareId(wareId)
                .setStock(skuNum)
                .setSkuName(skuName).setStockLocked(NumberConstants.ZERO);

//        2.首先判断当前库存是否存在
        LambdaQueryWrapper<WareSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WareSkuEntity::getSkuId, skuId).eq(WareSkuEntity::getWareId, wareId);
        WareSkuEntity wareSku = wareSkuDao.selectOne(queryWrapper);
        if (Objects.isNull(wareSku)) {
            wareSkuDao.insert(wareSkuEntity);
        } else {
            wareSkuEntity
                    .setId(wareSku.getId())
                    .setStock(wareSku.getStock() + skuNum);
            wareSkuDao.updateById(wareSkuEntity);
        }
        return R.ok();
    }

    /**
     * 是否有库存
     *
     * @param skuIds sku id
     * @return {@link List}<{@link SkuStockTO}>
     */
    @Override
    public List<SkuStockTO> hashStock(List<Long> skuIds) {
        List<SkuStockTO> skuStockTOList = skuIds.stream().map(skuId -> {
            SkuStockTO skuStockTO = new SkuStockTO();
            LambdaQueryWrapper<WareSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(WareSkuEntity::getSkuId, skuId);
            WareSkuEntity wareSku = wareSkuService.getOne(queryWrapper);
//            查询的对象为空表示当前sku没有库存
            if (Objects.isNull(wareSku)) {
                skuStockTO
                        .setSkuId(skuId)
                        .setHasStock(false);
                return skuStockTO;
            } else {
                skuStockTO
                        .setSkuId(skuId)
                        .setHasStock((wareSku.getStock() - wareSku.getStockLocked()) > 0);
                return skuStockTO;
            }
        }).collect(Collectors.toList());
        return skuStockTOList;
    }

    /**
     * 锁定库存
     *
     * @param wareSkuLockTO 器皿sku锁
     * @return {@link R}
     * 库存解锁的场景
     * 1.下单成功，但是订单过期被系统自动取消，或被用户手动取消
     * 2.下单成功，库存锁定成功，但是接下来的业务调用失败，导致订单回滚
     */
    @Transactional
    @Override
    public void wareSkuLock(WareSkuLockTO wareSkuLockTO) {
//        1.获取到订单项中的skuId查询哪些仓库有库存
        List<SkuWareIdList> skuWareIdLists = wareSkuLockTO.getOrderItemTOS().stream().map(orderItemTO -> {
            SkuWareIdList skuWareIdList = new SkuWareIdList();
            Long skuId = orderItemTO.getSkuId();
            Integer count = orderItemTO.getCount();
            List<Long> wareIds = wareSkuDao.wareListToHasStock(skuId, count);
            skuWareIdList.setSkuId(skuId)
                    .setCount(orderItemTO.getCount())
                    .setWareId(wareIds);
            return skuWareIdList;
        }).collect(Collectors.toList());

        for (SkuWareIdList skuWareId : skuWareIdLists) {
//        2.当前sku没有足够的库存
            if (CollectionUtils.isEmpty(skuWareId.getWareId())) {
                throw new UnEnoughStockException(skuWareId.getSkuId());
            }
//        3.锁定库存
            wareSkuDao.lockWare(skuWareId.getSkuId(), skuWareId.getWareId().get(0), skuWareId.getCount());

//        4.创建库存工作单对象
            WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
            wareOrderTaskEntity.setOrderSn(wareSkuLockTO.getOrderSn());
            wareOrderTaskService.save(wareOrderTaskEntity);

//        5.创建库存工作单详情对象
            WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
            wareOrderTaskDetailEntity.setSkuId(skuWareId.getSkuId())
                    .setWareId(skuWareId.getWareId().get(0))
                    .setSkuNum(skuWareId.getCount()).setLockStatus(1)
                    .setTaskId(wareOrderTaskEntity.getId());
            wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);

            StockDetailTO stockDetailTO = new StockDetailTO();
            BeanUtils.copyProperties(wareOrderTaskDetailEntity, stockDetailTO);
            StockLockedTO stockLockedTO = new StockLockedTO();
            stockLockedTO.setId(wareOrderTaskEntity.getId()).setDetail(stockDetailTO);

//         6.rabbitmq发送消息—当前一条库存锁定成功
            rabbitTemplate.convertAndSend(WareRmConstants.STOCK_EVENT_EXCHANGE,
                    WareRmConstants.STOCK_LOCKED_BINDING,
                    stockLockedTO
            );
        }
    }

    /**
     * 解锁库存
     *
     * @param stockLockedTO 解锁库存
     */
    @Override
    public void unlockedStock(StockLockedTO stockLockedTO) {
//        1.查询工作单是否存在
        WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.getById(stockLockedTO.getId());
        if (!Objects.isNull(wareOrderTaskEntity)) {
//        2.库存工作单不为空，根据订单id远程查询订单
            String orderSn = wareOrderTaskEntity.getOrderSn();
            OrderTO orderTO = orderFeignService.getOrderById(orderSn);
            if (Objects.isNull(orderTO) || orderTO.getStatus() == 4) {
//        3.是锁定状态才能够释放库存
                if (stockLockedTO.getDetail().getLockStatus() == 1){
//                3.1释放库存
                    wareSkuDao.releaseStock(stockLockedTO.getDetail().getSkuId(),
                            stockLockedTO.getDetail().getWareId(),
                            stockLockedTO.getDetail().getSkuNum());
//                3.2修改库存工作单详情的状态为解锁状态
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
                    wareOrderTaskDetailEntity.setId(stockLockedTO.getDetail().getId())
                            .setLockStatus(2);
                    wareOrderTaskDetailService.updateById(wareOrderTaskDetailEntity);
                }

            }
        }
    }

    /**
     * 防止订单服务卡顿，
     * 库存服务延迟队列中的消息先于订单服务的延迟队列被消费
     * 导致订单状态还未更改，状态未改变，库存不能释放。
     *
     * @param orderTO 以
     */
    @Transactional
    @Override
    public void unlockedStock(OrderTO orderTO) {
//        1.根据订单id查询库存工作单
        LambdaQueryWrapper<WareOrderTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WareOrderTaskEntity::getOrderSn,orderTO.getOrderSn());
        WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.getOne(queryWrapper);
        if (!Objects.isNull(wareOrderTaskEntity)){
//        2.按照工作单id查询没有解锁的库存工作单详情
            LambdaQueryWrapper<WareOrderTaskDetailEntity> detailQueryWrapper = new LambdaQueryWrapper<>();
            detailQueryWrapper.eq(WareOrderTaskDetailEntity::getTaskId,wareOrderTaskEntity.getId())
                    .eq(WareOrderTaskDetailEntity::getLockStatus,1);
            List<WareOrderTaskDetailEntity> wareOrderTaskDetailEntities = wareOrderTaskDetailDao.selectList(detailQueryWrapper);
            if (!CollectionUtils.isEmpty(wareOrderTaskDetailEntities)){
                for (WareOrderTaskDetailEntity wareOrderTaskDetailEntity : wareOrderTaskDetailEntities) {
                    StockDetailTO stockDetailTO = new StockDetailTO();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity,stockDetailTO);
                    StockLockedTO stockLockedTO = new StockLockedTO();
                    stockLockedTO.setId(wareOrderTaskEntity.getId())
                            .setDetail(stockDetailTO);
//        3.解锁库存
                    unlockedStock(stockLockedTO);
                }
            }
        }
    }

}
