package com.xha.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.constants.NumberConstants;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.product.dao.CategoryDao;
import com.xha.gulimall.product.entity.CategoryEntity;
import com.xha.gulimall.product.service.CategoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    private CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }


    /**
     * 得到产品类别树形结构
     *
     * @return {@link List}<{@link CategoryEntity}>
     */
    @Override
    public List<CategoryEntity> getProductCategoryListTree() {
//        1.查询到所有分类列表
        List<CategoryEntity> categorys = categoryDao.selectList(null);
//        2.将所有分类组装为树形结构
//          2.1获取到所有一级分类
        List<CategoryEntity> levelOneCategorys = categorys.stream()
                .filter(category -> category.getParentCid() == NumberConstants.TOP_LEVEL_CATEGORY)
                .collect(Collectors.toList());
//          2.2根据一级分类获取到其子分类
        List<CategoryEntity> productCategoryListTree = levelOneCategorys.stream()
                .map((levelOneCategory) -> {
                    levelOneCategory.setChildren(getChildrenCategory(levelOneCategory, categorys));
                    return levelOneCategory;
                })
//        3.对一级分类进行排序
                .sorted((levelOneCategory1, levelOneCategory2)
                        -> (levelOneCategory1.getSort() == null ? 0 : levelOneCategory1.getSort())
                        - (levelOneCategory2.getSort() == null ? 0 : levelOneCategory2.getSort()))
//        4.将流对象转换为List集合
                .collect(Collectors.toList());
        return productCategoryListTree;
    }

    /**
     * 批量逻辑删除分类
     *
     * @param categoryIds 正如列表
     */
    @Override
    public void removeCategoryByIds(List<Long> categoryIds) {
        //TODO 检查要删除的分类是否被其他地方引用
        categoryDao.deleteBatchIds(categoryIds);
    }

    /**
     * 根据一级分类获取到其子分类
     *
     * @param category     类别
     * @param categoryList 类别列表
     * @return {@link List}<{@link CategoryEntity}>
     */
    public List<CategoryEntity> getChildrenCategory(CategoryEntity category, List<CategoryEntity> categoryList) {
//        1.获取到当前一级分类的子分类
        List<CategoryEntity> childrenCategoryList = categoryList.stream()
                .filter(categorys -> categorys.getParentCid() == category.getCatId())
//        2.递归查询子分类的子分类
                .map((categorys) -> {
                    categorys.setChildren(getChildrenCategory(categorys, categoryList));
                    return categorys;
                })
//        3.对子分类进行排序
                .sorted((category1, category2)
                        -> (category1.getSort()) == null ? 0 : category1.getSort()
                        - (category2.getSort() == null ? 0 : category2.getSort()))
                .collect(Collectors.toList());
        return childrenCategoryList;
    }

}



