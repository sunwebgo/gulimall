package com.xha.gulimall.product.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.constants.NumberConstants;
import com.xha.gulimall.common.to.SkuReductionTO;
import com.xha.gulimall.common.to.SpuBoundTO;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.product.dao.SpuInfoDao;
import com.xha.gulimall.product.dto.spusavedto.*;
import com.xha.gulimall.product.entity.*;
import com.xha.gulimall.product.feign.CouponFeignService;
import com.xha.gulimall.product.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Resource
    private SpuInfoDescService spuInfoDescService;

    @Resource
    private SpuImagesService spuImagesService;

    @Resource
    private AttrService attrService;

    @Resource
    private SkuInfoService skuInfoService;

    @Resource
    private SkuImagesService skuImagesService;

    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Resource
    private ProductAttrValueService productAttrValueService;

    @Resource
    private CouponFeignService couponFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存商品信息
     *
     * @param spuSaveDTO spu拯救dto
     * @return {@link R}
     */
    @Transactional
    @Override
    public R saveSpuInfo(SpuSaveDTO spuSaveDTO) {
//        1.保存spu的基本信息 pms_spu_info
//          1.1将SpuSaveDTO对象转换成SpuInfoEntity对象
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveDTO, spuInfoEntity);
//          1.2根据spu_name判断当前spu是否存在
        if (!Objects.isNull(getOne(new LambdaQueryWrapper<SpuInfoEntity>()
                .eq(SpuInfoEntity::getSpuName, spuInfoEntity.getSpuName())))) {
            return R.error().put("msg", "当前商品已添加");
        }
//          1.3保存SpuInfoEntity对象
        save(spuInfoEntity);

//        2.保存spu的描述图片信息 pms_spu_info_desc
//          1.2在DTO对象中获取到spu的描述信息
        List<String> decript = spuSaveDTO.getDecript();
        if (!CollectionUtils.isEmpty(decript)) {
            SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
            spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
            spuInfoDescEntity.setDecript(String.join(",", decript));
//          1.3保存spu的描述信息
            spuInfoDescService.save(spuInfoDescEntity);
        }

//        3.保存spu的图片集 pms_spu_images
//          3.1在DTO对象当中获取到spu的图片信息
        List<String> images = spuSaveDTO.getImages();
        if (!CollectionUtils.isEmpty(images)) {
            List<SpuImagesEntity> spuImageLists = images.stream().map((image) -> {
                SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
                spuImagesEntity.setSpuId(spuInfoEntity.getId());
                spuImagesEntity.setImgUrl(image);
                return spuImagesEntity;
            }).collect(Collectors.toList());
            spuImagesService.saveBatch(spuImageLists);
        }

//        4.保存spu的基本属性  pms_product_attr_value
//          4.1在DTO对象当中获取到基本属性信息
        List<BaseAttrs> baseAttrs = spuSaveDTO.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            List<ProductAttrValueEntity> productAttrValueList = baseAttrs.stream().map((baseAttr) -> {
                ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
//                4.1.1根据属性id获取到属性名
                String attrName = attrService.getById(baseAttr.getAttrId()).getAttrName();
                return productAttrValueEntity
                        .setSpuId(spuInfoEntity.getId())
                        .setAttrId(baseAttr.getAttrId())
                        .setAttrName(attrName)
                        .setAttrValue(baseAttr.getAttrValues())
                        .setQuickShow(baseAttr.getShowDesc());
            }).collect(Collectors.toList());
            productAttrValueService.saveBatch(productAttrValueList);

        }
//        5.保存当前spu的sku信息
//          5.1获取到当前spu的sku的信息
        List<Skus> skus = spuSaveDTO.getSkus();
//          5.2保存sku的基本信息 pms_sku_info
//              5.2.1将Skus对象转换为SkuInfo对象
        List<SkuInfoEntity> skuInfoList = skus.stream().map((sku) -> {
            String defaultImage = "";
            for (Images image : sku.getImages()) {
                if (image.getDefaultImg() == NumberConstants.DEFAULT_IMAGE) {
                    defaultImage = image.getImgUrl();
                }
            }
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
//            生成sku_id
            Long skuId = Long.valueOf(RandomUtil.randomNumbers(NumberConstants.RANDOM_NUMBER_SIZE));

            BeanUtils.copyProperties(sku, skuInfoEntity);
            skuInfoEntity
                    .setSkuId(skuId)
                    .setBrandId(spuInfoEntity.getBrandId())
                    .setCatelogId(spuInfoEntity.getCatelogId())
                    .setSaleCount(0L).setSpuId(spuInfoEntity.getId())
                    .setSkuDefaultImg(defaultImage);

//          5.2保存sku的图片信息 pms_sku_images
//              5.2.1在skus中获取到images信息
            List<Images> imagesList = sku.getImages();
            List<SkuImagesEntity> skuImagesEntityList = imagesList.stream()
//              5.2.2剔除图片为空的情况
                    .filter(skuImage -> !StringUtils.isEmpty(skuImage.getImgUrl()))
                    .map((skuImage) -> {
//              5.2.3将Images对象转换为SkuImages对象
                        SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                        BeanUtils.copyProperties(skuImage, skuImagesEntity);
                        skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());
                        return skuImagesEntity;
                    }).collect(Collectors.toList());
//              5.2.4保存sku的图片信息
            skuImagesService.saveBatch(skuImagesEntityList);

//          5.3保存sku的销售属性 pms_sku_sale_attr_value
//              5.3.1获取到sku的销售属性
            List<Attr> attrList = sku.getAttr();
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntityList = attrList.stream().map(attr -> {
//              5.3.2将attr对象转换为SkuSalAttrValueEntity对象
                SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                return skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
            }).collect(Collectors.toList());
            skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntityList);


//        7.保存sku的优惠满减信息 gulimall_sms
            SkuReductionTO skuReductionTO = new SkuReductionTO();
//            7.1将sku中的信息转换为SkuReductionTO对象
            BeanUtils.copyProperties(sku, skuReductionTO);
            skuReductionTO.setSkuId(skuInfoEntity.getSkuId());
//            7.2剔除满0件打0折的情况
            if (skuReductionTO.getFullCount() > NumberConstants.ZERO
                    || skuReductionTO.getFullPrice().compareTo(BigDecimal.ZERO) == 1) {
//            7.3通过openfegin调用gulimall-coupon模块
                couponFeignService.saveSkuReduction(skuReductionTO);
            }

            return skuInfoEntity;
        }).collect(Collectors.toList());
        skuInfoService.saveBatch(skuInfoList);

//        6.保存spu的积分信息 gulimall_sms -> sms_sku_ladder....
//          6.1在DTO当中获取到Bounds对象
        Bounds bounds = spuSaveDTO.getBounds();
        SpuBoundTO spuBoundTO = new SpuBoundTO();
//          6.2将Bounds对象转换为SpuBoundTO对象
        BeanUtils.copyProperties(bounds, spuBoundTO);
        spuBoundTO.setSpuId(spuInfoEntity.getId());
//          6.3通过openfegin调用gulimall-coupon模块
        couponFeignService.saveSpuBound(spuBoundTO);

        return R.ok();
    }

    /**
     * 根据条件查询spu信息
     *
     * @param params 参数个数
     * @return {@link PageUtils}
     */
    @Override
    public PageUtils queryPageByConditation(Map<String, Object> params) {
        LambdaQueryWrapper<SpuInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        Object catelogId = params.get("catelogId");
        Object brandId = params.get("brandId");
        String status = (String) params.get("status");
        String key = (String) params.get("key");


//        1.分类
        if (!Objects.isNull(catelogId) && !catelogId.equals(String.valueOf(NumberConstants.ZERO))) {
            queryWrapper.eq(SpuInfoEntity::getCatelogId, catelogId);
        }
//        2.品牌
        if (!Objects.isNull(brandId) && !brandId.equals(String.valueOf(NumberConstants.ZERO))) {
            queryWrapper.eq(SpuInfoEntity::getBrandId, brandId);
        }
//        3.状态
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq(SpuInfoEntity::getPublishStatus, status);
        }
//        4.检索关键字
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrapper -> wrapper
                    .eq(SpuInfoEntity::getId, key)
                    .or()
                    .like(SpuInfoEntity::getSpuName, key));
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }


}
