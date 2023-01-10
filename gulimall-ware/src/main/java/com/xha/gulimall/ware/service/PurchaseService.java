package com.xha.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.ware.dto.MergePurchaseDTO;
import com.xha.gulimall.ware.dto.PurchaseDoneDTO;
import com.xha.gulimall.ware.entity.PurchaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:47:49
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceivePurchase(Map<String, Object> params);

    R mergePurchaseTable(MergePurchaseDTO mergePurchaseDTO);

    R received(List<Long> ids);

    R finishPurchase(PurchaseDoneDTO purchaseDoneDTO);
}

