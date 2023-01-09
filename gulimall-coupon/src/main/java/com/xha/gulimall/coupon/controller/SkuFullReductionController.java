package com.xha.gulimall.coupon.controller;

import java.util.Arrays;
import java.util.Map;

import com.xha.gulimall.common.to.SkuReductionTO;
import org.springframework.web.bind.annotation.*;

import com.xha.gulimall.coupon.entity.SkuFullReductionEntity;
import com.xha.gulimall.coupon.service.SkuFullReductionService;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.R;
import javax.annotation.Resource;


/**
 * 商品满减信息
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:43:17
 */
@RestController
@RequestMapping("coupon/skufullreduction")
public class SkuFullReductionController {
    @Resource
    private SkuFullReductionService skuFullReductionService;


    /**
     * 保存sku打折满减等信息
     *
     * @param skuReductionTO sku减少
     * @return {@link R}
     */
    @PostMapping("/saveskureduction")
    public R saveSkuReduction(@RequestBody SkuReductionTO skuReductionTO){
        skuFullReductionService.saveSkuReduction(skuReductionTO);
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
//    @RequiresPermissions("coupon:skufullreduction:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = skuFullReductionService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
//    @RequiresPermissions("coupon:skufullreduction:info")
    public R info(@PathVariable("id") Long id){
		SkuFullReductionEntity skuFullReduction = skuFullReductionService.getById(id);

        return R.ok().put("skuFullReduction", skuFullReduction);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//    @RequiresPermissions("coupon:skufullreduction:save")
    public R save(@RequestBody SkuFullReductionEntity skuFullReduction){
		skuFullReductionService.save(skuFullReduction);

        return R.ok();
    }




    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("coupon:skufullreduction:update")
    public R update(@RequestBody SkuFullReductionEntity skuFullReduction){
		skuFullReductionService.updateById(skuFullReduction);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("coupon:skufullreduction:delete")
    public R delete(@RequestBody Long[] ids){
		skuFullReductionService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
