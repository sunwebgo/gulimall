package com.xha.gulimall.product.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.constants.NumberConstants;
import com.xha.gulimall.common.enums.ProductEnums;
import com.xha.gulimall.common.to.product.SkuReductionTO;
import com.xha.gulimall.common.to.product.SkuStockTO;
import com.xha.gulimall.common.to.product.SpuBoundTO;
import com.xha.gulimall.common.to.product.SpuInfoTO;
import com.xha.gulimall.common.to.es.AttrES;
import com.xha.gulimall.common.to.es.SkuInfoES;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.product.dao.ProductAttrValueDao;
import com.xha.gulimall.product.dao.SpuInfoDao;
import com.xha.gulimall.product.dto.spusavedto.*;
import com.xha.gulimall.product.entity.*;
import com.xha.gulimall.product.feign.CouponFeignService;
import com.xha.gulimall.product.feign.SearchFeignService;
import com.xha.gulimall.product.feign.WareFeignService;
import com.xha.gulimall.product.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
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
    private BrandService brandService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private SkuInfoService skuInfoService;

    @Resource
    private SkuImagesService skuImagesService;

    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Resource
    private ProductAttrValueService productAttrValueService;

    @Resource
    private ProductAttrValueDao productAttrValueDao;

    @Resource
    private CouponFeignService couponFeignService;

    @Resource
    private WareFeignService wareFeignService;

    @Resource
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * ??????????????????
     *
     * @param spuSaveDTO spu??????dto
     * @return {@link R}
     */
    @GlobalTransactional
    @Override
    public R saveSpuInfo(SpuSaveDTO spuSaveDTO) {
//        1.??????spu??????????????? pms_spu_info
//          1.1???SpuSaveDTO???????????????SpuInfoEntity??????
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveDTO, spuInfoEntity);
//          1.2??????spu_name????????????spu????????????
        if (!Objects.isNull(getOne(new LambdaQueryWrapper<SpuInfoEntity>()
                .eq(SpuInfoEntity::getSpuName, spuInfoEntity.getSpuName())))) {
            return R.error().put("msg", "?????????????????????");
        }
//          1.3??????SpuInfoEntity??????
        save(spuInfoEntity);

//        2.??????spu????????????????????? pms_spu_info_desc
//          1.2???DTO??????????????????spu???????????????
        List<String> decript = spuSaveDTO.getDecript();
        if (!CollectionUtils.isEmpty(decript)) {
            SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
            spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
            spuInfoDescEntity.setDecript(String.join(",", decript));
//          1.3??????spu???????????????
            spuInfoDescService.save(spuInfoDescEntity);
        }

//        3.??????spu???????????? pms_spu_images
//          3.1???DTO?????????????????????spu???????????????
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

//        4.??????spu???????????????  pms_product_attr_value
//          4.1???DTO???????????????????????????????????????
        List<BaseAttrs> baseAttrs = spuSaveDTO.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            List<ProductAttrValueEntity> productAttrValueList = baseAttrs.stream().map((baseAttr) -> {
                ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
//                4.1.1????????????id??????????????????
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
//        5.????????????spu???sku??????
//          5.1???????????????spu???sku?????????
        List<Skus> skus = spuSaveDTO.getSkus();
//          5.2??????sku??????????????? pms_sku_info
//              5.2.1???Skus???????????????SkuInfo??????
        List<SkuInfoEntity> skuInfoList = skus.stream().map((sku) -> {
            String defaultImage = "";
            for (Images image : sku.getImages()) {
                if (image.getDefaultImg() == NumberConstants.DEFAULT_IMAGE) {
                    defaultImage = image.getImgUrl();
                }
            }
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
//            ??????sku_id
            Long skuId = Long.valueOf(RandomUtil.randomNumbers(NumberConstants.RANDOM_NUMBER_SIZE));

            BeanUtils.copyProperties(sku, skuInfoEntity);
            skuInfoEntity
                    .setSkuId(skuId)
                    .setBrandId(spuInfoEntity.getBrandId())
                    .setCatelogId(spuInfoEntity.getCatelogId())
                    .setSaleCount(0L).setSpuId(spuInfoEntity.getId())
                    .setSkuDefaultImg(defaultImage);

//          5.2??????sku??????????????? pms_sku_images
//              5.2.1???skus????????????images??????
            List<Images> imagesList = sku.getImages();
            List<SkuImagesEntity> skuImagesEntityList = imagesList.stream()
//              5.2.2???????????????????????????
                    .filter(skuImage -> !StringUtils.isEmpty(skuImage.getImgUrl()))
                    .map((skuImage) -> {
//              5.2.3???Images???????????????SkuImages??????
                        SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                        BeanUtils.copyProperties(skuImage, skuImagesEntity);
                        skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());
                        return skuImagesEntity;
                    }).collect(Collectors.toList());
//              5.2.4??????sku???????????????
            skuImagesService.saveBatch(skuImagesEntityList);

//          5.3??????sku??????????????? pms_sku_sale_attr_value
//              5.3.1?????????sku???????????????
            List<Attr> attrList = sku.getAttr();
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntityList = attrList.stream().map(attr -> {
//              5.3.2???attr???????????????SkuSalAttrValueEntity??????
                SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                return skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
            }).collect(Collectors.toList());
            skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntityList);


//        7.??????sku????????????????????? gulimall_sms
            SkuReductionTO skuReductionTO = new SkuReductionTO();
//            7.1???sku?????????????????????SkuReductionTO??????
            BeanUtils.copyProperties(sku, skuReductionTO);
            skuReductionTO.setSkuId(skuInfoEntity.getSkuId());
//            7.2?????????0??????0????????????
            if (skuReductionTO.getFullCount() > NumberConstants.ZERO
                    || skuReductionTO.getFullPrice().compareTo(BigDecimal.ZERO) == 1) {
//            7.3??????openfegin??????gulimall-coupon??????
                couponFeignService.saveSkuReduction(skuReductionTO);
            }

            return skuInfoEntity;
        }).collect(Collectors.toList());
        skuInfoService.saveBatch(skuInfoList);

//        6.??????spu??????????????? gulimall_sms -> sms_sku_ladder....
//          6.1???DTO???????????????Bounds??????
        Bounds bounds = spuSaveDTO.getBounds();
        SpuBoundTO spuBoundTO = new SpuBoundTO();
//          6.2???Bounds???????????????SpuBoundTO??????
        BeanUtils.copyProperties(bounds, spuBoundTO);
        spuBoundTO.setSpuId(spuInfoEntity.getId());
//          6.3??????openfegin??????gulimall-coupon??????
        couponFeignService.saveSpuBound(spuBoundTO);

        return R.ok();
    }

    /**
     * ??????????????????spu??????
     *
     * @param params ????????????
     * @return {@link PageUtils}
     */
    @Override
    public PageUtils queryPageByConditation(Map<String, Object> params) {
        LambdaQueryWrapper<SpuInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        Object catelogId = params.get("catelogId");
        Object brandId = params.get("brandId");
        String status = (String) params.get("status");
        String key = (String) params.get("key");


//        1.??????
        if (!Objects.isNull(catelogId) && !catelogId.equals(String.valueOf(NumberConstants.ZERO))) {
            queryWrapper.eq(SpuInfoEntity::getCatelogId, catelogId);
        }
//        2.??????
        if (!Objects.isNull(brandId) && !brandId.equals(String.valueOf(NumberConstants.ZERO))) {
            queryWrapper.eq(SpuInfoEntity::getBrandId, brandId);
        }
//        3.??????
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq(SpuInfoEntity::getPublishStatus, status);
        }
//        4.???????????????
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

    /**
     * ????????????
     *
     * @return {@link R}
     */
    @Override
    public R upProduct(Long spuId) {
//        1.???????????????spu?????????attr????????????
        LambdaQueryWrapper<ProductAttrValueEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductAttrValueEntity::getSpuId, spuId);
        List<ProductAttrValueEntity> attrList = productAttrValueDao.selectList(queryWrapper);

//        2.???ProductAttrValueEntity???????????????AttrES??????
        List<AttrES> attrESList = attrList.stream().map(attr -> {
            AttrES attrES = new AttrES();
            BeanUtils.copyProperties(attr, attrES);
            return attrES;
        }).collect(Collectors.toList());


//        3.??????spuId??????????????????sku??????
        List<SkuInfoEntity> skuInfoList = skuInfoService.getSkuInfo(spuId);
//        4.??????sku??????????????????
        if (skuInfoList.isEmpty()) {
            return R.error().put("msg", "??????spuID??????????????????sku??????");
        }

//        5.?????????skuId??????
        List<Long> skuIds = skuInfoList.stream().map(skuInfo -> {
            return skuInfo.getSkuId();
        }).collect(Collectors.toList());

//        6.???sku???????????????SkuInfoES??????
        List<SkuInfoES> upProducts = skuInfoList.stream().map(sku -> {
            SkuInfoES SkuInfoES = new SkuInfoES();
            BeanUtils.copyProperties(sku, SkuInfoES);
            SkuInfoES.setSkuTitle(sku.getSkuName())
                    .setSkuPrice(sku.getPrice())
                    .setSkuImg(sku.getSkuDefaultImg())
                    .setHotScore(NumberConstants.HOT_SCORE);

//       7.????????????
            Map<Long, Boolean> hasStock = null;
            try {
//        7.1.??????????????????????????????sku???????????????
                List<SkuStockTO> skuStockTOS = wareFeignService.hasStock(skuIds);
                hasStock = skuStockTOS.stream()
//                    6.1???skuID?????????????????????????????????map??????
                        .collect(Collectors.toMap(skuStockTO -> skuStockTO.getSkuId(),
                                skuStockTO -> skuStockTO.getHasStock()));
            } catch (Exception e) {
                log.error("????????????????????????" + e.getMessage());
            }

            Map<Long, Boolean> finalHasStock = hasStock;
            SkuInfoES.setHasStock(finalHasStock.get(sku.getSkuId()));

//        8.??????????????????????????????
            BrandEntity brand = brandService.getById(sku.getBrandId());
            if (!Objects.isNull(brand)) {
                SkuInfoES.setBrandName(brand.getName()).setBrandImg(brand.getLogo());
            }
//        9.?????????????????????
            CategoryEntity category = categoryService.getById(sku.getCatelogId());
            if (!Objects.isNull(category)) {
                SkuInfoES.setCatelogName(category.getName());
            }

//        10.????????????????????????
            SkuInfoES.setAttrs(attrESList);
            return SkuInfoES;

        }).collect(Collectors.toList());

//        11.??????gulimall-search????????????????????????es
        boolean result = searchFeignService.upProduct(upProducts);

        if (result) {
            return R.error().put("msg", "es??????????????????");
        }
//        12.???????????????spu??????,????????????
        SpuInfoEntity spu = getById(spuId);
        spu.setPublishStatus(ProductEnums.PUBLISH_STATUS_DROP.getValue());
//        13.??????spu????????????
        updateById(spu);
        return R.ok().put("msg", "es??????????????????");
    }

    @Override
    public SpuInfoTO getSpuInfo(Long skuId) {
        SpuInfoTO spuInfoTO = new SpuInfoTO();
//        1.??????skuId??????spu??????
        SpuInfoEntity spuInfoEntity = getById(skuInfoService.getById(skuId).getSpuId());
//        2.?????????spu???????????????
        String brandName = brandService.getById(spuInfoEntity.getBrandId()).getName();
        spuInfoTO.setSpuId(spuInfoEntity.getId())
                .setSpuName(spuInfoEntity.getSpuName())
                .setSpuBrand(brandName);
        return spuInfoTO;
    }
}
