package com.xha.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.constants.NumberConstants;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.product.dao.SkuInfoDao;
import com.xha.gulimall.product.entity.SkuInfoEntity;
import com.xha.gulimall.product.entity.SpuInfoEntity;
import com.xha.gulimall.product.service.SkuInfoService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

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


}
