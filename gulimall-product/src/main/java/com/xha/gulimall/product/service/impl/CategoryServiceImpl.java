package com.xha.gulimall.product.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xha.gulimall.common.constants.NumberConstants;
import com.xha.gulimall.common.constants.RedisConstants;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.product.dao.CategoryDao;
import com.xha.gulimall.product.entity.CategoryEntity;
import com.xha.gulimall.product.service.CategoryBrandRelationService;
import com.xha.gulimall.product.service.CategoryService;
import com.xha.gulimall.product.vo.Catelog2VO;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    private CategoryDao categoryDao;

    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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
     * 根据分类id查询到分类id路径
     *
     * @param catelogId catelog id
     * @return {@link Long[]}
     */
    @Override
    public Long[] findCatelogId(Long catelogId) {
        List<Long> catelogPath = new ArrayList<>();
//        1.调用方法获取到分类id集合
        List<Long> parentPath = getParentPath(catelogId, catelogPath);
//        2.反转集合
        Collections.reverse(parentPath);
        return catelogPath.toArray(new Long[0]);
    }

    /**
     * 得到父id
     *
     * @param catelogId   catelog id
     * @param catelogPath catelog路径
     * @return {@link List}<{@link Long}>
     */
    public List<Long> getParentPath(Long catelogId, List<Long> catelogPath) {
//        1.将分类id添加到集合中
        catelogPath.add(catelogId);
//        2.根据分类id查询到分类的详细信息
        CategoryEntity category = getById(catelogId);
//        3.判断此分类是否还有父分类
        if (category.getParentCid() != NumberConstants.TOP_LEVEL_CATEGORY) {
//            3.1如果有父分类就继续查询
            getParentPath(category.getParentCid(), catelogPath);
        }
        return catelogPath;
    }

    /**
     * 同步修改
     *
     * @param category 类别
     */
    @Transactional
    @Override
    public void updateDetails(CategoryEntity category) {
        updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
//        2.同步更新其他关联表中的数据
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

//        3.TODO 更新其他关联表
        }
    }


    /**
     * 查询出所有的一级分类
     *
     * @return {@link List}<{@link CategoryEntity}>
     */
    @Override
    public List<CategoryEntity> getFirstCategory() {
        LambdaQueryWrapper<CategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CategoryEntity::getParentCid, NumberConstants.TOP_LEVEL_CATEGORY);
        List<CategoryEntity> firstCategory = categoryDao.selectList(queryWrapper);
        return firstCategory;
    }

    /**
     * 获取到分类的JSON数据
     *
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    @Override
    public Map<String, List<Catelog2VO>> getCatalogJson() {
//        1.首先查询缓存
        String categoryList = stringRedisTemplate.opsForValue().get(RedisConstants.CATEGORY_TREE_CACHE);
//        2.如果缓存未命中就查询数据库，重建缓存
        if (StringUtils.isEmpty(categoryList)){
//            2.1查询数据库
            Map<String, List<Catelog2VO>> categoryByPermanent = getCategoryByPermanent();
//            2.2重建缓存
            stringRedisTemplate.opsForValue().set(RedisConstants.CATEGORY_TREE_CACHE,
                    JSONUtil.toJsonStr(categoryByPermanent));
            return categoryByPermanent;
        }
//        3.缓存命中就反序列化为Map对象
        return JSONUtil.toBean(categoryList, Map.class);
    }


    /**
     * 查询数据库获取到数据
     *
     * @return {@link Map}<{@link String}, {@link List}<{@link Catelog2VO}>>
     */
    public Map<String, List<Catelog2VO>> getCategoryByPermanent() {
        //        1.查询到所有分类列表
        List<CategoryEntity> categorys = categoryDao.selectList(null);

//        2.获取到所有一级分类
        List<CategoryEntity> levelOneCategorys = getChildCategoryList(categorys, Long.valueOf(NumberConstants.TOP_LEVEL_CATEGORY));

//        3.遍历一级分类,获取到当前一级分类的子分类
        Map<String, List<Catelog2VO>> categoryList = levelOneCategorys
                .stream()
                .collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
//            3.1获取到当前一级分类的二级分类
                    List<CategoryEntity> secondChildList = getChildCategoryList(categorys, v.getCatId());

//            3.2将category对象封装为对应的Catelog2VO对象
                    List<Catelog2VO> catelog2VOS = null;
                    if (!Objects.isNull(secondChildList)) {
                        catelog2VOS = secondChildList.stream().map(secondChild -> {
                            Catelog2VO catelog2VO = new Catelog2VO(
                                    secondChild.getCatId().toString(),
                                    secondChild.getName(),
                                    secondChild.getParentCid().toString(),
                                    null);

//                    3.2.1获取到当前分类的三级分类
                            List<CategoryEntity> thirdChildList = getChildCategoryList(categorys, secondChild.getCatId());

//                    3.2.2将category对象封装为对应的Catelog3VO对象
                            if (!Objects.isNull(thirdChildList)) {
                                List<Catelog2VO.Catelog3VO> catelog3VOS = thirdChildList.stream().map(thirdChild -> {
                                    return new Catelog2VO.Catelog3VO(
                                            secondChild.getCatId().toString(),
                                            thirdChild.getCatId().toString(),
                                            thirdChild.getName());
                                }).collect(Collectors.toList());
                                catelog2VO.setCatalog3List(catelog3VOS);
                            }
                            return catelog2VO;
                        }).collect(Collectors.toList());
                    }
                    return catelog2VOS;
                }));

        return categoryList;
    }

    /**
     * 获取到子分类列表
     *
     * @param categorys 思想史范畴
     * @param parentId  父id
     * @return {@link List}<{@link CategoryEntity}>
     */
    private List<CategoryEntity> getChildCategoryList(List<CategoryEntity> categorys, Long parentId) {
        return categorys.stream().filter(category -> {
            return category.getParentCid() == parentId;
        }).collect(Collectors.toList());
    }

}



