package com.xha.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.constants.NumberConstants;
import com.xha.gulimall.common.to.SkuStockTO;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.ware.dao.WareSkuDao;
import com.xha.gulimall.ware.entity.WareSkuEntity;
import com.xha.gulimall.ware.feign.ProductFeign;
import com.xha.gulimall.ware.service.WareSkuService;
import org.springframework.stereotype.Service;
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
    private ProductFeign productFeign;

    @Resource
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
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
        String skuName = productFeign.getSkuName(skuId);
        if (skuName.equals("当前sku信息不存在")){
            return R.error().put("msg","当前sku信息不存在");
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
}
