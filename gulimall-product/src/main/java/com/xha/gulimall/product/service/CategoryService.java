package com.xha.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.product.entity.CategoryEntity;
import com.xha.gulimall.product.vo.Catelog2VO;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:39:19
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);


    List<CategoryEntity> getProductCategoryListTree();

    void removeCategoryByIds(List<Long> categoryIds);

    Long[] findCatelogId(Long catelogId);

    void updateDetails(CategoryEntity category);

    List<CategoryEntity> getFirstCategory();

    Map<String, List<Catelog2VO>> getCatalogJson();

}

