package com.xha.gulimall.ware.controller;

import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.ware.dto.MergePurchaseDTO;
import com.xha.gulimall.ware.dto.PurchaseDoneDTO;
import com.xha.gulimall.ware.entity.PurchaseEntity;
import com.xha.gulimall.ware.service.PurchaseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 采购信息
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:47:49
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Resource
    private PurchaseService purchaseService;

    /**
     * 查询未领取的采购单
     *
     * @param params 参数个数
     * @return {@link R}
     */
    @RequestMapping("/unreceive/list")
//    @RequiresPermissions("ware:purchase:list")
    public R queryPageUnreceivePurchase(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPageUnreceivePurchase(params);

        return R.ok().put("page", page);
    }

    /**
     * 合并采购单
     *
     * @return {@link R}
     */
    @RequestMapping("/merge")
    public R mergePurchaseTable(@RequestBody MergePurchaseDTO mergePurchaseDTO){
       return purchaseService.mergePurchaseTable(mergePurchaseDTO);
    }


    /**
     * 领取采购单
     *
     * @param ids id
     * @return {@link R}
     */
    @RequestMapping("/received")
    public R received(@RequestBody List<Long> ids){
        return purchaseService.received(ids);
    }

    /**
     * 完成采购
     *
     * @param purchaseDoneDTO 购买完成dto
     * @return {@link R}
     */
    @PostMapping("/done")
    public R finishPurchase(@RequestBody PurchaseDoneDTO purchaseDoneDTO){
        return purchaseService.finishPurchase(purchaseDoneDTO);
    }

    /**
     * 查询采购列表
     *
     * @param params 参数个数
     * @return {@link R}
     */
    @RequestMapping("/list")
//    @RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
//    @RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//    @RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase){
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
