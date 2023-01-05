package com.xha.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.product.entity.SkuInfoEntity;

import java.util.Map;

/**
 * sku信息
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:39:19
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}
