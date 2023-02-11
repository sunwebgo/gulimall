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
     * 保存商品信息
     *
     * @param spuSaveDTO spu拯救dto
     * @return {@link R}
     */
    @GlobalTransactional
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

    /**
     * 上传商品
     *
     * @return {@link R}
     */
    @Override
    public R upProduct(Long spuId) {
//        1.获取到当前spu关联的attr信息列表
        LambdaQueryWrapper<ProductAttrValueEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductAttrValueEntity::getSpuId, spuId);
        List<ProductAttrValueEntity> attrList = productAttrValueDao.selectList(queryWrapper);

//        2.将ProductAttrValueEntity对象转换为AttrES对象
        List<AttrES> attrESList = attrList.stream().map(attr -> {
            AttrES attrES = new AttrES();
            BeanUtils.copyProperties(attr, attrES);
            return attrES;
        }).collect(Collectors.toList());


//        3.根据spuId查询出对应的sku信息
        List<SkuInfoEntity> skuInfoList = skuInfoService.getSkuInfo(spuId);
//        4.判断sku集合是否为空
        if (skuInfoList.isEmpty()) {
            return R.error().put("msg", "当前spuID不存在对应的sku信息");
        }

//        5.获取到skuId列表
        List<Long> skuIds = skuInfoList.stream().map(skuInfo -> {
            return skuInfo.getSkuId();
        }).collect(Collectors.toList());

//        6.将sku对象转换为SkuInfoES对象
        List<SkuInfoES> upProducts = skuInfoList.stream().map(sku -> {
            SkuInfoES SkuInfoES = new SkuInfoES();
            BeanUtils.copyProperties(sku, SkuInfoES);
            SkuInfoES.setSkuTitle(sku.getSkuName())
                    .setSkuPrice(sku.getPrice())
                    .setSkuImg(sku.getSkuDefaultImg())
                    .setHotScore(NumberConstants.HOT_SCORE);

//       7.设置库存
            Map<Long, Boolean> hasStock = null;
            try {
//        7.1.调用库存服务查询当前sku是否有库存
                List<SkuStockTO> skuStockTOS = wareFeignService.hasStock(skuIds);
                hasStock = skuStockTOS.stream()
//                    6.1将skuID对应的是否有库存转换成map集合
                        .collect(Collectors.toMap(skuStockTO -> skuStockTO.getSkuId(),
                                skuStockTO -> skuStockTO.getHasStock()));
            } catch (Exception e) {
                log.error("查询库存量异常：" + e.getMessage());
            }

            Map<Long, Boolean> finalHasStock = hasStock;
            SkuInfoES.setHasStock(finalHasStock.get(sku.getSkuId()));

//        8.查询品牌名和品牌图片
            BrandEntity brand = brandService.getById(sku.getBrandId());
            if (!Objects.isNull(brand)) {
                SkuInfoES.setBrandName(brand.getName()).setBrandImg(brand.getLogo());
            }
//        9.查询分类的名字
            CategoryEntity category = categoryService.getById(sku.getCatelogId());
            if (!Objects.isNull(category)) {
                SkuInfoES.setCatelogName(category.getName());
            }

//        10.设置基本属性列表
            SkuInfoES.setAttrs(attrESList);
            return SkuInfoES;

        }).collect(Collectors.toList());

//        11.调用gulimall-search模块，保存数据到es
        boolean result = searchFeignService.upProduct(upProducts);

        if (result) {
            return R.error().put("msg", "es存储数据失败");
        }
//        12.获取到当前spu对象,更改状态
        SpuInfoEntity spu = getById(spuId);
        spu.setPublishStatus(ProductEnums.PUBLISH_STATUS_DROP.getValue());
//        13.更改spu上架状态
        updateById(spu);
        return R.ok().put("msg", "es存储数据成功");
    }

    @Override
    public SpuInfoTO getSpuInfo(Long skuId) {
        SpuInfoTO spuInfoTO = new SpuInfoTO();
//        1.根据skuId查询spu信息
        SpuInfoEntity spuInfoEntity = getById(skuInfoService.getById(skuId).getSpuId());
//        2.获取到spu的品牌信息
        String brandName = brandService.getById(spuInfoEntity.getBrandId()).getName();
        spuInfoTO.setSpuId(spuInfoEntity.getId())
                .setSpuName(spuInfoEntity.getSpuName())
                .setSpuBrand(brandName);
        return spuInfoTO;
    }
}
