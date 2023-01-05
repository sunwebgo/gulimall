package com.xha.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.Query;
import com.xha.gulimall.product.dao.AttrGroupDao;
import com.xha.gulimall.product.entity.AttrGroupEntity;
import com.xha.gulimall.product.service.AttrGroupService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

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
//             2.2检索字段可能查询属性分组的id或名字
                queryWrapper.and(obj -> obj.eq(AttrGroupEntity::getAttrGroupId, key)
                        .or()
                        .like(AttrGroupEntity::getAttrGroupName, key));
            }
            IPage<AttrGroupEntity> page =
                    this.page(new Query<AttrGroupEntity>().getPage(params), queryWrapper);
            return new PageUtils(page);
        }
    }
}
