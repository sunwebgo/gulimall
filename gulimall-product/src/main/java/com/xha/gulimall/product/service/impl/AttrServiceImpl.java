package com.xha.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.constants.NumberConstants;
import com.xha.gulimall.common.enums.ProductEnums;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.product.dao.*;
import com.xha.gulimall.product.dto.AttrDTO;
import com.xha.gulimall.product.entity.*;
import com.xha.gulimall.product.service.AttrAttrgroupRelationService;
import com.xha.gulimall.product.service.AttrService;
import com.xha.gulimall.product.vo.AttrVO;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Resource
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Resource
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Resource
    private ProductAttrValueDao productAttrValueDao;

    @Resource
    private CategoryDao categoryDao;

    @Resource
    private AttrGroupDao attrGroupDao;


    @Override
    public PageUtils queryPage(Map<String, Object> params, String attrType, Long catelogId) {
//        1.获取到检索字段
        String key = (String) params.get("key");
        LambdaQueryWrapper<AttrEntity> queryWrapper =
                new LambdaQueryWrapper<>();
        queryWrapper.eq(AttrEntity::getAttrType, attrType.equalsIgnoreCase("base") ? 1 : 0);
        IPage<AttrEntity> page;
//        2.分页查询所有属性
        if (catelogId == 0 && StringUtils.isEmpty(key)) {
            page =
                    this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        } else if (catelogId == 0 && !StringUtils.isEmpty(key)) {
            queryWrapper.eq(AttrEntity::getAttrId, key)
                    .or()
                    .like(AttrEntity::getAttrName, key);
            page =
                    this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        } else {
//         3.根据查询条件查询对应的分类的属性
            queryWrapper.eq(AttrEntity::getCatelogId, catelogId);
            if (!StringUtils.isEmpty(key)) {
//             3.1检索字段可能查询属性分组的id或名字
                queryWrapper.and(obj -> obj.eq(AttrEntity::getAttrId, key)
                        .or()
                        .like(AttrEntity::getAttrName, key));
            }
            page =
                    this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        }

        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> attrLists = page.getRecords();
        List<AttrVO> attrVOS = attrLists.stream().map((attr) -> {
//            1.将AttrEntity对象转换为AttrVO对象
            AttrVO attrVO = new AttrVO();
            BeanUtils.copyProperties(attr, attrVO);
//            2.查询AttrVO对象的分类名
            CategoryEntity categoryEntity = categoryDao.selectById(attrVO.getCatelogId());
            attrVO.setCatelogName(categoryEntity.getName());
//            3.查询AttrVO对象的属性分组名
            LambdaQueryWrapper<AttrAttrgroupRelationEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId());
//            4.首先根据attrId在属性-属性分组表中查询到对应的AttrAttrgroupRelationEntity对象

//            5.再根据attrGroupId在属性分组表中查询到对应的AttrGroupEntity对象
            AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrVO.getAttrGroupId());
            if (!Objects.isNull(attrGroupEntity)) {
                attrVO.setGroupName(attrGroupEntity.getAttrGroupName());
            }
            System.out.println(attrVO);
            return attrVO;
        }).collect(Collectors.toList());

        pageUtils.setList(attrVOS);
        return pageUtils;
    }

    /**
     * 修改属性信息
     *
     * @param attr attr
     * @return {@link R}
     */
    @Override
    public R updateAttr(AttrDTO attr) {
//        1.将AttrDTO对象转换为AttrEntity对象
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
//        2.查询当前属性是否存在
        AttrEntity getAttr = getById(attrEntity);
        if (Objects.isNull(getAttr)) {
            return R.error().put("msg", "当前修改的属性不存在");
        }

//        3.修改属性信息
        updateById(attrEntity);
//        4.级联更新对应的属性-属性分组表
        if (attrEntity.getAttrType() == ProductEnums.ATTR_TYPE_BASE.getValue()) {
//            4.1首先判断要修改的关联关系是否存在
            if (!Objects.isNull(attrAttrgroupRelationService.getOne(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                    .eq(AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId())))) {
                LambdaUpdateWrapper<AttrAttrgroupRelationEntity> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(AttrAttrgroupRelationEntity::getAttrGroupId, attrEntity.getAttrGroupId())
                        .eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId());
                attrAttrgroupRelationService.update(updateWrapper);
            } else {
                AttrAttrgroupRelationEntity attrAttrgroupRelation = new AttrAttrgroupRelationEntity();
                attrAttrgroupRelation.setAttrId(attr.getAttrId());
                attrAttrgroupRelation.setAttrGroupId(attr.getAttrGroupId());
                attrAttrgroupRelationService.save(attrAttrgroupRelation);
            }
        }
        return R.ok();
    }

    @GlobalTransactional
    @Override
    public void saveAttr(AttrDTO attr) {
        AttrEntity attrEntity = new AttrEntity();
//        1.将AttrVO对象转换为AttrEntity对象
        BeanUtils.copyProperties(attr, attrEntity);
        save(attrEntity);
//        2.只有基本属性有属性分组
        if (attrEntity.getAttrType().equals(ProductEnums.ATTR_TYPE_BASE.getComment())) {
//        3.向属性分组关联表中保存属性和属性分组的关联关系
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    /**
     * 得到attr详细信息
     *
     * @param attrId attr id
     * @return {@link AttrEntity}
     */
    @Override
    public R getAttrDetailsInfo(Long attrId) {
//        1.首先根据attrId查询到AttrEntity对象的详细信息
        AttrEntity attrEntity = getById(attrId);
        if (Objects.isNull(attrEntity)) {
            return R.error().put("msg", "当前属性不存在");
        }
//        2.将AttrEntity对象转换为AttrVo对象
        AttrVO attrVO = new AttrVO();
        BeanUtils.copyProperties(attrEntity, attrVO);
//        3.根据分类id获取到跟分类的完整路径
        List<Long> categoryIds = new ArrayList<>();
        List<Long> categoryIdPath = getCategoryIdPath(attrVO.getCatelogId(), categoryIds);
        Collections.reverse(categoryIdPath);
        attrVO.setCatelogPath(categoryIdPath.toArray(new Long[0]));
        return R.ok().put("attr", attrVO);
    }


    /**
     * 删除attr
     *
     * @param attrIds attr id
     * @return {@link R}
     */
    @Override
    public R deleteAttr(Long[] attrIds) {
        List<Long> attrLists = Arrays.asList(attrIds);
        for (Long attrId : attrLists) {
            AttrEntity attr = getById(attrId);
//            1.当属性存在时才进行删除
            if (!Objects.isNull(attr)) {
                removeById(attrId);
//            2.只有当时基本属性的时候才会级联更新属性-属性分组表
                if (attr.getAttrType().equals(ProductEnums.ATTR_TYPE_BASE.getComment())) {
                    LambdaQueryWrapper<AttrAttrgroupRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(AttrAttrgroupRelationEntity::getAttrId, attrId);
                    attrAttrgroupRelationDao.delete(queryWrapper);
                }
            } else {
                return R.ok().put("msg", "含有不存在的属性");
            }
        }
        return R.ok().put("msg", "删除属性成功");
    }

    /**
     * 得到类别id路径
     *
     * @param catelogId   catelog id
     * @param categoryIds 类别id
     * @return {@link List}<{@link Long}>
     */
    private List<Long> getCategoryIdPath(Long catelogId, List<Long> categoryIds) {
        categoryIds.add(catelogId);
//        1.根据分类id查询当前分类
        CategoryEntity category = categoryDao.selectById(catelogId);
        if (category.getParentCid() != NumberConstants.TOP_LEVEL_CATEGORY) {
            getCategoryIdPath(category.getParentCid(), categoryIds);
        }
        return categoryIds;
    }

    /**
     * 获取到squ的基本属性
     *
     * @return {@link R}
     */
    @Override
    public R getSpuAttrBySpuId(Long spuId) {
        LambdaQueryWrapper<ProductAttrValueEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductAttrValueEntity::getSpuId, spuId);
        List<ProductAttrValueEntity> productAttrValueLists = productAttrValueDao.selectList(queryWrapper);
        if (Objects.isNull(productAttrValueLists)) {
            return R.error().put("msg", "当前spu不存在");
        }
        return R.ok().put("data", productAttrValueLists);
    }

    /**
     * 修改spu的基本属性
     *
     * @param spuId spu id
     * @return {@link R}
     */
    @Override
    public R updateSpuAttrBySpuId(Long spuId, List<ProductAttrValueEntity> pavList) {
        List<ProductAttrValueEntity> productAttrValueEntityList = productAttrValueDao.selectList(new LambdaQueryWrapper<ProductAttrValueEntity>()
                .eq(ProductAttrValueEntity::getSpuId, spuId));
        if (productAttrValueEntityList.isEmpty()){
            return R.error().put("msg","不存在当前spu的基本属性信息");
        }
        for (ProductAttrValueEntity pav : pavList) {
            LambdaQueryWrapper<ProductAttrValueEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    .eq(ProductAttrValueEntity::getSpuId, spuId)
                    .eq(ProductAttrValueEntity::getAttrId, pav.getAttrId());
            pav.setSpuId(spuId);
            productAttrValueDao.update(pav, queryWrapper);
        }
        return R.ok();
    }

}
