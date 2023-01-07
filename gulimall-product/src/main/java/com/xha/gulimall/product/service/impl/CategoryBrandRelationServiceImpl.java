package com.xha.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.product.dao.BrandDao;
import com.xha.gulimall.product.dao.CategoryBrandRelationDao;
import com.xha.gulimall.product.dao.CategoryDao;
import com.xha.gulimall.product.entity.BrandEntity;
import com.xha.gulimall.product.entity.CategoryBrandRelationEntity;
import com.xha.gulimall.product.entity.CategoryEntity;
import com.xha.gulimall.product.service.CategoryBrandRelationService;
import com.xha.gulimall.product.vo.CategoryBrandVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Resource
    private CategoryBrandRelationDao categoryBrandRelationDao;

    @Resource
    private BrandDao brandDao;

    @Resource
    private CategoryDao categoryDao;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 获取品牌关联的分类
     *
     * @param brandId 品牌标识
     * @return {@link List}<{@link CategoryBrandRelationEntity}>
     */
    @Override
    public List<CategoryBrandRelationEntity> getBrandToCategorylist(Long brandId) {
        LambdaQueryWrapper<CategoryBrandRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CategoryBrandRelationEntity::getBrandId, brandId);
        List<CategoryBrandRelationEntity> brandToCategorylists = categoryBrandRelationDao.selectList(queryWrapper);
        return brandToCategorylists;
    }

    @Override
    public void saveDetails(CategoryBrandRelationEntity categoryBrandRelation) {
//        1.获取到品牌id和分类id
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
//        2.查询品牌名和分类名
        BrandEntity brandEntity = brandDao.selectById(brandId);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());
        save(categoryBrandRelation);
    }

    /**
     * 同步更新品牌名
     *
     * @param brandId 品牌标识
     * @param name    名字
     */
    @Override
    public void updateBrand(Long brandId, String name) {
        LambdaQueryWrapper<CategoryBrandRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CategoryBrandRelationEntity::getBrandId, brandId);
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setBrandName(name);
        update(categoryBrandRelationEntity, queryWrapper);
    }

    /**
     * 同步更新类别
     *
     * @param catId 猫id
     * @param name  名字
     */
    @Override
    public void updateCategory(Long catId, String name) {
        LambdaQueryWrapper<CategoryBrandRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CategoryBrandRelationEntity::getCatelogId, catId);
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setCatelogName(name);
        update(categoryBrandRelationEntity, queryWrapper);
    }

    /**
     * 获取分类关联的品牌列表
     *
     * @param catId 猫id
     * @return {@link R}
     */
    @Override
    public R getCategoryBrandRelationList(Long catId) {
        LambdaQueryWrapper<CategoryBrandRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CategoryBrandRelationEntity::getCatelogId, catId);
//        1.根据分类d查询分类品牌关联表
        List<CategoryBrandRelationEntity> categoryBrandRelationList = categoryBrandRelationDao.selectList(queryWrapper);
        List<CategoryBrandVO> categoryBrandVOList = null;
        if (categoryBrandRelationList.size() > 0) {

            categoryBrandVOList = categoryBrandRelationList.stream()
                    .map((categoryBrand -> {
//        2.将CategoryBrandRelationEntity对象转换为CategoryBrandVO对象
                        CategoryBrandVO categoryBrandVO = new CategoryBrandVO();
                        BeanUtils.copyProperties(categoryBrand, categoryBrandVO);
                        return categoryBrandVO;
                    })).collect(Collectors.toList());
        } else {
            return R.error().put("msg","当前分类下暂无品牌");
        }
        return R.ok().put("data", categoryBrandVOList);
    }

}
