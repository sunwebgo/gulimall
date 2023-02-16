package com.xha.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.constants.NumberConstants;
import com.xha.gulimall.common.to.coupon.SeckillSkuRelationTO;
import com.xha.gulimall.common.to.product.SkuInfoTO;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.product.dao.SkuInfoDao;
import com.xha.gulimall.product.entity.SkuImagesEntity;
import com.xha.gulimall.product.entity.SkuInfoEntity;
import com.xha.gulimall.product.entity.SpuInfoDescEntity;
import com.xha.gulimall.product.feign.SeckillFeignService;
import com.xha.gulimall.product.service.*;
import com.xha.gulimall.product.vo.SkuItemSaleAttrVO;
import com.xha.gulimall.product.vo.SkuItemVO;
import com.xha.gulimall.product.vo.SpuItemAttrGroupVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {


    @Resource
    private SkuInfoDao skuInfoDao;

    @Resource
    private SkuImagesService skuImagesService;

    @Resource
    private SpuInfoDescService spuInfoDescService;

    @Resource
    private AttrGroupService attrGroupService;

    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据条件查询sku信息
     *
     * @param params 参数个数
     * @return {@link PageUtils}
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<SkuInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        Object catelogId = params.get("catelogId");
        Object brandId = params.get("brandId");
        String key = (String) params.get("key");
        Object min = params.get("min");
        Object max = params.get("max");


//        1.分类
        if (!Objects.isNull(catelogId) && !catelogId.equals(String.valueOf(NumberConstants.ZERO))) {
            queryWrapper.eq(SkuInfoEntity::getCatelogId, catelogId);
        }
//        2.品牌
        if (!Objects.isNull(brandId) && !brandId.equals(String.valueOf(NumberConstants.ZERO))) {
            queryWrapper.eq(SkuInfoEntity::getBrandId, brandId);
        }
//        3.价钱最小值
        if (!Objects.isNull(min) && !min.equals(String.valueOf(NumberConstants.ZERO))) {
            queryWrapper.ge(SkuInfoEntity::getPrice, min);
        }
//        4.价钱最大值
        if (!Objects.isNull(max) && !max.equals(String.valueOf(NumberConstants.ZERO))) {
            queryWrapper.le(SkuInfoEntity::getPrice, max);
        }
//        4.检索关键字
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrapper -> wrapper
                    .eq(SkuInfoEntity::getSkuId, key)
                    .or()
                    .like(SkuInfoEntity::getSkuName, key));
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    /**
     * 根据spuID得到sku信息
     *
     * @param spuId spu id
     * @return {@link List}<{@link SkuInfoEntity}>
     */
    @Override
    public List<SkuInfoEntity> getSkuInfo(Long spuId) {
        LambdaQueryWrapper<SkuInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuInfoEntity::getSpuId, spuId);
        List<SkuInfoEntity> skuInfoList = skuInfoDao.selectList(queryWrapper);
        if (skuInfoList.isEmpty()) {
            return null;
        }
        return skuInfoList;
    }


    /**
     * 根据skuID获取到sku的详细信息
     *
     * @param skuId sku id
     * @return {@link SkuItemVO}
     */
    @Override
    public SkuItemVO getSkuItemInfo(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVO skuItemVO = new SkuItemVO();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
//        1.获取到sku的基本信息 pms_sku_info
            SkuInfoEntity skuInfo = getById(skuId);
            skuItemVO.setSkuInfoEntity(skuInfo);
            return skuInfo;
        }, threadPoolExecutor);

//        2.以下几个任务都依赖于infoFuture的执行结果

//        3.获取spu的介绍
        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((result) -> {
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(result.getSpuId());
            skuItemVO.setDesp(spuInfoDescEntity);
        }, threadPoolExecutor);

//        4.获取spu的基本属性信息
        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((result) -> {
            List<SpuItemAttrGroupVO> spuItemAttrGroupVOS = attrGroupService
                    .getAttrGroupWithAttrsBySpuId(result.getSpuId());
            skuItemVO.setGroupVos(spuItemAttrGroupVOS);
        }, threadPoolExecutor);

//        5.获取到spu的销售属性组合
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((result) -> {
            List<SkuItemSaleAttrVO> saleAttrVOS =
                    skuSaleAttrValueService.getSaleAttrBySpuId(result.getSpuId());
            skuItemVO.setSaleAttr(saleAttrVOS);
        }, threadPoolExecutor);


//        6.获取到sku的图片信息 pms_sku_images
        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> skuImageInfo = skuImagesService.getSkuImageInfo(skuId);
            skuItemVO.setImages(skuImageInfo);
        });

//        7.查询当前sku是否有秒杀活动
        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            R seckillInfo = seckillFeignService.getSeckillInfoBySkuId(skuId);
            if (seckillInfo.getCode() == 0) {
                SeckillSkuRelationTO seckillSkuRelationTO = seckillInfo.getData(new TypeReference<SeckillSkuRelationTO>() {
                });
                skuItemVO.setSeckillSkuRelationTO(seckillSkuRelationTO);
            }
        },threadPoolExecutor);


//        等待所有任务完成
        CompletableFuture
                .allOf(descFuture, baseAttrFuture, saleAttrFuture, imageFuture, seckillFuture)
                .get();
        return skuItemVO;
    }

    /**
     * 得到所有sku信息列表
     *
     * @return {@link List}<{@link SkuInfoEntity}>
     */
    @Override
    public List<SkuInfoTO> getAllSkuInfoList() {
        List<SkuInfoEntity> skuInfoEntityList = skuInfoDao.selectList(null);
        List<SkuInfoTO> skuInfoTOList = skuInfoEntityList.stream()
                .map(skuInfoEntity -> {
                    SkuInfoTO skuInfoTO = new SkuInfoTO();
                    BeanUtils.copyProperties(skuInfoEntity, skuInfoTO);
                    return skuInfoTO;
                })
                .collect(Collectors.toList());
        return skuInfoTOList;
    }
}
