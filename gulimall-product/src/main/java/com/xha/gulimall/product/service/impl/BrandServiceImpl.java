package com.xha.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.product.dao.BrandDao;
import com.xha.gulimall.product.entity.BrandEntity;
import com.xha.gulimall.product.service.BrandService;
import com.xha.gulimall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
//        1.获取到查询的条件
        String key = (String) params.get("key");
        LambdaQueryWrapper<BrandEntity> queryWrapper = new LambdaQueryWrapper<>();
//        2。不为空就按条件查询
        if (!StringUtils.isEmpty(key)){
            queryWrapper.eq(BrandEntity::getBrandId,key).or().like(BrandEntity::getName,key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public R updateDetails(BrandEntity brand) {
//        1.判断数据库中是否存在当前品牌
        BrandEntity brandEntity = getById(brand);
        if (Objects.isNull(brandEntity)){
            return R.error().put("msg","当前品牌不存在");
        }
//        1.保证冗余字段的数据一致性
        updateById(brand);
        if (!StringUtils.isEmpty(brand.getName())){
//        2.同步更新其他关联表中的数据
            categoryBrandRelationService.updateBrand(brand.getBrandId(),brand.getName());
//        3.TODO 更新其他关联表
        }
        return R.ok().put("msg","品牌更新完成");
    }

}
