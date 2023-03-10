package com.xha.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.product.dto.AttrDTO;
import com.xha.gulimall.product.entity.AttrEntity;
import com.xha.gulimall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:39:19
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params, String attrType, Long catelogId);

    R updateAttr(AttrDTO attr);

    void saveAttr(AttrDTO attr);

    R getAttrDetailsInfo(Long attrId);

    R deleteAttr(Long[] attrIds);

    R getSpuAttrBySpuId(Long spuId);

    R updateSpuAttrBySpuId(Long spuId, List<ProductAttrValueEntity> pavList);
}

