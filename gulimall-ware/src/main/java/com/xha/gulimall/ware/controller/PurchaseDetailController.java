package com.xha.gulimall.ware.controller;

import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.ware.entity.PurchaseDetailEntity;
import com.xha.gulimall.ware.service.PurchaseDetailService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;


/**
 *
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:47:49
 */
@RestController
@RequestMapping("ware/purchasedetail")
public class PurchaseDetailController {
    @Resource
    private PurchaseDetailService purchaseDetailService;


    /**
     *查询采购需求
     *
     * @param params 参数个数
     * @return {@link R}
     */
    @RequestMapping("/list")
//    @RequiresPermissions("ware:purchasedetail:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseDetailService.queryPageByConsition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
//    @RequiresPermissions("ware:purchasedetail:info")
    public R info(@PathVariable("id") Long id){
		PurchaseDetailEntity purchaseDetail = purchaseDetailService.getById(id);

        return R.ok().put("purchaseDetail", purchaseDetail);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//    @RequiresPermissions("ware:purchasedetail:save")
    public R save(@RequestBody PurchaseDetailEntity purchaseDetail){
		purchaseDetailService.save(purchaseDetail);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("ware:purchasedetail:update")
    public R update(@RequestBody PurchaseDetailEntity purchaseDetail){
		purchaseDetailService.updateById(purchaseDetail);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("ware:purchasedetail:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseDetailService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
