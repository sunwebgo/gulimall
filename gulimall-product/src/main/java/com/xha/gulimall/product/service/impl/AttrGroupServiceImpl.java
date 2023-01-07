package com.xha.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.enums.ProductEnums;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.xha.gulimall.product.dao.AttrDao;
import com.xha.gulimall.product.dao.AttrGroupDao;
import com.xha.gulimall.product.dto.AttrGroupDTO;
import com.xha.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.xha.gulimall.product.entity.AttrEntity;
import com.xha.gulimall.product.entity.AttrGroupEntity;
import com.xha.gulimall.product.service.AttrAttrgroupRelationService;
import com.xha.gulimall.product.service.AttrGroupService;
import com.xha.gulimall.product.service.AttrService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {


    @Resource
    private AttrGroupDao attrGroupDao;

    @Resource
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Resource
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Resource
    private AttrDao attrDao;

    @Resource
    private AttrService attrService;

    /**
     * 获取分组属性分组
     *
     * @param params
     * @param
     * @return
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
//        1.获取到检索字段
        String key = (String) params.get("key");
        LambdaQueryWrapper<AttrGroupEntity> queryWrapper = new LambdaQueryWrapper<>();
//        2.分页查询所有分类的分组属性
        if (catelogId == 0 && StringUtils.isEmpty(key)) {
            IPage<AttrGroupEntity> page =
                    this.page(new Query<AttrGroupEntity>().getPage(params), new QueryWrapper<AttrGroupEntity>());
            return new PageUtils(page);
        } else if (catelogId == 0 && !StringUtils.isEmpty(key)) {
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttrGroupEntity::getAttrGroupId, key)
                    .or()
                    .like(AttrGroupEntity::getAttrGroupName, key);
            IPage<AttrGroupEntity> page =
                    this.page(new Query<AttrGroupEntity>().getPage(params), queryWrapper);
            return new PageUtils(page);
        } else {
//         3.根据查询条件查询对应的分类的分组属性
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttrGroupEntity::getCatelogId, catelogId);
            if (!StringUtils.isEmpty(key)) {
//             3.1检索字段可能查询属性分组的id或名字
                queryWrapper.and(obj -> obj.eq(AttrGroupEntity::getAttrGroupId, key)
                        .or()
                        .like(AttrGroupEntity::getAttrGroupName, key));
            }
            IPage<AttrGroupEntity> page =
                    this.page(new Query<AttrGroupEntity>().getPage(params), queryWrapper);
            return new PageUtils(page);
        }
    }

    /**
     * 删除属性分组
     *
     * @param attrGroupIds attr组id
     * @return {@link R}
     */
    @Override
    public R deleteAttrGroups(Long[] attrGroupIds) {
        List<Long> attrGroupIdList = Arrays.stream(attrGroupIds).collect(Collectors.toList());
//        1.判断属性分组是否存在
        for (Long attrId : attrGroupIdList) {
            if (Objects.isNull(getById(attrId))) {
                return R.error().put("msg", "含有不存在的属性分组");
            }
        }
        removeByIds(attrGroupIdList);
        return R.ok().put("msg", "删除成功");
    }

    /**
     * 查询属性-属性分组的关联关系
     *
     * @param attrGroupId attr组id
     * @return {@link R}
     */
    @Override
    public R getAttrGroupRelation(Long attrGroupId) {
//        1.首先根据attrGroupId查询到AttrAttrgroupRelationEntity对象列表
        LambdaQueryWrapper<AttrAttrgroupRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttrAttrgroupRelationEntity::getAttrGroupId, attrGroupId);
        List<AttrAttrgroupRelationEntity> relations = attrAttrgroupRelationDao.selectList(queryWrapper);
//        2.判断是否存在关联关系
        if (Objects.isNull(relations)) {
            return R.error().put("msg", "不存在关联关系");
        }
//        2.查询到Attr对象列表
        List<AttrEntity> attrLists = relations.stream().map((relation) -> {
            AttrEntity attrEntity = attrDao.
                    selectOne(new LambdaQueryWrapper<AttrEntity>()
                            .eq(AttrEntity::getAttrId, relation.getAttrId()));
            return attrEntity;
        }).collect(Collectors.toList());
        return R.ok().put("data", attrLists);
    }

    /**
     * 删除属性分组和属性的关联关系
     *
     * @param attrGroupDTO attr集团dto
     * @return {@link R}
     */
    @Override
    public R removeAttrGroupRelation(AttrGroupDTO[] attrGroupDTO) {
//        1.将数组中的每一条数据封装为一条 AttrAttrgroupRelationEntity 对象
        List<AttrAttrgroupRelationEntity> relationLists = Arrays.stream(attrGroupDTO)
                .map((attrAttrGroup) -> {
                    AttrAttrgroupRelationEntity attrAttrgroupRelation = new AttrAttrgroupRelationEntity();
                    BeanUtils.copyProperties(attrAttrGroup, attrAttrgroupRelation);
                    return attrAttrgroupRelation;
                }).collect(Collectors.toList());

        for (AttrAttrgroupRelationEntity relation : relationLists) {
            LambdaQueryWrapper<AttrAttrgroupRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttrAttrgroupRelationEntity::getAttrId, relation.getAttrId())
                    .eq(AttrAttrgroupRelationEntity::getAttrGroupId, relation.getAttrGroupId());
            if (Objects.isNull(attrAttrgroupRelationDao.selectOne(queryWrapper))) {
                return R.error().put("msg", "含有不存在的关联关系");
            }
            //        2.将当前属性的属性分组id置为空
            AttrEntity attr = attrService.getById(relation.getAttrId());
            attrService.update(new LambdaUpdateWrapper<AttrEntity>()
                    .set(AttrEntity::getAttrGroupId,null).eq(AttrEntity::getAttrId,attr.getAttrId()));
            attrAttrgroupRelationDao.delete(queryWrapper);

        }
        return R.ok();
    }

    /**
     * 获取属性分组没有关联的其他属性
     *
     * @return {@link R}
     */
    @Override
    public PageUtils getAttrGroupNoRelation(Long attrGroupId, Map<String, Object> params) {
//        1.当前分组只能关联自己所属的分类的属性
        AttrGroupEntity attrGroup = getById(attrGroupId);
//          1.2获取到分类id
        Long catelogId = attrGroup.getCatelogId();
//        2.当前属性分组只能关联分类下没有分组的属性
//          2.1获取到当前分类下的所有分组
        List<AttrGroupEntity> attrGroupLists = attrGroupDao.selectList(new LambdaQueryWrapper<AttrGroupEntity>()
                .eq(AttrGroupEntity::getCatelogId, catelogId));
//          2.2获取到属性分组列表的att_group_id属性，用以在中间关系表中查询
        List<Long> attrGroupIds = attrGroupLists.stream()
                .map(attrGroupList -> attrGroupList.getAttrGroupId())
                .collect(Collectors.toList());
//          2.3在中间关系表中查询已经关联过的属性id列表
        List<AttrAttrgroupRelationEntity> relationLists = attrAttrgroupRelationDao.selectList(
                new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
                        .in(AttrAttrgroupRelationEntity::getAttrGroupId, attrGroupIds));
        List<Long> attrIdLists = relationLists.stream()
                .map(relationList -> relationList.getAttrId())
                .collect(Collectors.toList());

//        3.查询当前分组可以关联的属性
        LambdaQueryWrapper<AttrEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
//                3.1必须是当前分类
                .eq(AttrEntity::getCatelogId, catelogId)
//                3.2必须要是基本属性
                .eq(AttrEntity::getAttrType, ProductEnums.ATTR_TYPE_BASE.getValue());
//                3.3不能有已经关联过的属性
        if (!Objects.isNull(attrIdLists) && attrIdLists.size() > 0) {
            queryWrapper.notIn(AttrEntity::getAttrId, attrIdLists);
        }
//        4.添加检索条件查询
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq(AttrEntity::getAttrId, key).or().eq(AttrEntity::getAttrName, key);
        }
        IPage<AttrEntity> page = attrService.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    /**
     * 添加属性分组和属性的关联关系
     *
     * @param attrGroupDTO attr集团dto
     * @return {@link R}
     */
    @Override
    public R addAttrAndAttrGroupRelation(AttrGroupDTO[] attrGroupDTO) {
//        1.将AttrGroupDTO对象转换为
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationLists = Arrays.stream(attrGroupDTO).map((attrAttrGroup) -> {
            AttrAttrgroupRelationEntity attrAttrgroupRelation = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(attrAttrGroup, attrAttrgroupRelation);
//        2.将当前属性的属性分组id修改为当前的属性分组
            AttrEntity attr = attrService.getById(attrAttrgroupRelation.getAttrId());
            attr.setAttrGroupId(attrAttrgroupRelation.getAttrGroupId());
            attrService.updateById(attr);
            return attrAttrgroupRelation;
        }).collect(Collectors.toList());

        attrAttrgroupRelationService.saveBatch(attrAttrgroupRelationLists);
        return R.ok();
    }

}
