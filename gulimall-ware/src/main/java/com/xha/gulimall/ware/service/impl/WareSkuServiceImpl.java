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
     * ????????????????????????????????????sku??????
     *
     * @param params ????????????
     * @return {@link R}
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        LambdaQueryWrapper<WareSkuEntity> queryWrapper = new LambdaQueryWrapper<>();

//        1.????????????????????????
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
     * ????????????
     *
     * @param skuId  sku id
     * @param wareId ??????id
     * @param skuNum sku num
     */
    @Override
    public R addStock(Long skuId, Long wareId, Integer skuNum) {
//        1.??????skuID??????sku??????
        String skuName = productFeignService.getSkuName(skuId);
        if (skuName.equals("??????sku???????????????")) {
            return R.error().put("msg", "??????sku???????????????");
        }

//        2.??????WareSkuEntity??????
        WareSkuEntity wareSkuEntity = new WareSkuEntity();
        wareSkuEntity
                .setSkuId(skuId)
                .setWareId(wareId)
                .setStock(skuNum)
                .setSkuName(skuName).setStockLocked(NumberConstants.ZERO);

//        2.????????????????????????????????????
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
     * ???????????????
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
//            ?????????????????????????????????sku????????????
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
     * ????????????
     *
     * @param wareSkuLockTO ??????sku???
     * @return {@link R}
     * ?????????????????????
     * 1.?????????????????????????????????????????????????????????????????????????????????
     * 2.?????????????????????????????????????????????????????????????????????????????????????????????
     */
    @Transactional
    @Override
    public void wareSkuLock(WareSkuLockTO wareSkuLockTO) {
//        1.????????????????????????skuId???????????????????????????
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
//        2.??????sku?????????????????????
            if (CollectionUtils.isEmpty(skuWareId.getWareId())) {
                throw new UnEnoughStockException(skuWareId.getSkuId());
            }
//        3.????????????
            wareSkuDao.lockWare(skuWareId.getSkuId(), skuWareId.getWareId().get(0), skuWareId.getCount());

//        4.???????????????????????????
            WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
            wareOrderTaskEntity.setOrderSn(wareSkuLockTO.getOrderSn());
            wareOrderTaskService.save(wareOrderTaskEntity);

//        5.?????????????????????????????????
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

//         6.rabbitmq?????????????????????????????????????????????
            rabbitTemplate.convertAndSend(WareRmConstants.STOCK_EVENT_EXCHANGE,
                    WareRmConstants.STOCK_LOCKED_BINDING,
                    stockLockedTO
            );
        }
    }

    /**
     * ????????????
     *
     * @param stockLockedTO ????????????
     */
    @Override
    public void unlockedStock(StockLockedTO stockLockedTO) {
//        1.???????????????????????????
        WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.getById(stockLockedTO.getId());
        if (!Objects.isNull(wareOrderTaskEntity)) {
//        2.???????????????????????????????????????id??????????????????
            String orderSn = wareOrderTaskEntity.getOrderSn();
            OrderTO orderTO = orderFeignService.getOrderById(orderSn);
            if (Objects.isNull(orderTO) || orderTO.getStatus() == 4) {
//        3.????????????????????????????????????
                if (stockLockedTO.getDetail().getLockStatus() == 1){
//                3.1????????????
                    wareSkuDao.releaseStock(stockLockedTO.getDetail().getSkuId(),
                            stockLockedTO.getDetail().getWareId(),
                            stockLockedTO.getDetail().getSkuNum());
//                3.2???????????????????????????????????????????????????
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
                    wareOrderTaskDetailEntity.setId(stockLockedTO.getDetail().getId())
                            .setLockStatus(2);
                    wareOrderTaskDetailService.updateById(wareOrderTaskDetailEntity);
                }

            }
        }
    }

    /**
     * ???????????????????????????
     * ??????????????????????????????????????????????????????????????????????????????
     * ????????????????????????????????????????????????????????????????????????
     *
     * @param orderTO ???
     */
    @Transactional
    @Override
    public void unlockedStock(OrderTO orderTO) {
//        1.????????????id?????????????????????
        LambdaQueryWrapper<WareOrderTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WareOrderTaskEntity::getOrderSn,orderTO.getOrderSn());
        WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.getOne(queryWrapper);
        if (!Objects.isNull(wareOrderTaskEntity)){
//        2.???????????????id??????????????????????????????????????????
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
//        3.????????????
                    unlockedStock(stockLockedTO);
                }
            }
        }
    }

}
