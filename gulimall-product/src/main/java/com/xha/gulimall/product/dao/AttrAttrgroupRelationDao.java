package com.xha.gulimall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xha.gulimall.product.entity.AttrAttrgroupRelationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 属性&属性分组关联
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:39:19
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    /**
     * 更新attr集团
     *
     * @param attrId      attr id
     * @param attrGroupId attr组id
     */
    void updateAttrGroup(Long attrId, Long attrGroupId);
}
