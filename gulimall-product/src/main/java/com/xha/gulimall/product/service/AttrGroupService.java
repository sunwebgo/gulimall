package com.xha.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.product.dto.AttrGroupDTO;
import com.xha.gulimall.product.entity.AttrGroupEntity;

import java.util.Map;

/**
 * 属性分组
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:39:19
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {


    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    R deleteAttrGroups(Long[] attrGroupIds);

    R getAttrGroupRelation(Long attrGroupId);

    R removeAttrGroupRelation(AttrGroupDTO[] attrGroupDTO);

    PageUtils getAttrGroupNoRelation(Long attrGroupId, Map<String, Object> params);

    R addAttrAndAttrGroupRelation(AttrGroupDTO[] attrGroupDTO);

    R getCategoryAttrGroup(Long catelogId);
}

