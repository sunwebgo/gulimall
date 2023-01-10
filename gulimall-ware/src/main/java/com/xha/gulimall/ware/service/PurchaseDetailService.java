package com.xha.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.ware.entity.PurchaseDetailEntity;
import com.xha.gulimall.ware.entity.PurchaseEntity;

import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:47:49
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageByConsition(Map<String, Object> params);

    List<PurchaseDetailEntity> listDetailByPurcharsById(Long id);
}

