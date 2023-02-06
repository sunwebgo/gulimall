package com.xha.gulimall.product.controller;

import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.product.dto.spusavedto.SpuSaveDTO;
import com.xha.gulimall.product.entity.SkuInfoEntity;
import com.xha.gulimall.product.service.SkuInfoService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;


/**
 * sku信息
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:39:19
 */
@RestController
@RequestMapping("product/skuinfo")
public class SkuInfoController {
    @Resource
    private SkuInfoService skuInfoService;

    @GetMapping("/price/{skuId}")
    public R getSkuPrice(@PathVariable("skuId") Long skuId) {
        SkuInfoEntity skuInfo = skuInfoService.getById(skuId);
        return R.ok().setData(skuInfo.getPrice().toString());
    }


    /**
     * 根据条件查询sku信息
     *
     * @param params 参数个数
     * @return {@link R}
     */
    @RequestMapping("/list")
//    @RequiresPermissions("product:skuinfo:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = skuInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{skuId}")
//    @RequiresPermissions("product:skuinfo:info")
    public R getSkuInfo(@PathVariable("skuId") Long skuId) {
        SkuInfoEntity skuInfo = skuInfoService.getById(skuId);
        return R.ok().put("skuInfo", skuInfo);
    }


    /**
     * 信息
     */
    @RequestMapping("/getskuname/{skuId}")
//    @RequiresPermissions("product:skuinfo:info")
    public String getSkuName(@PathVariable("skuId") Long skuId) {
        SkuInfoEntity skuInfo = skuInfoService.getById(skuId);
        if (!Objects.isNull(skuInfo)) {
            return skuInfo.getSkuName();
        } else {
            return "当前sku信息不存在";
        }
    }

    /**
     * 保存商品信息
     *
     * @param spuSaveDTO spu拯救dto
     * @return {@link R}
     */
    @RequestMapping("/save")
//    @RequiresPermissions("product:skuinfo:save")
    public R saveSpuInfo(@RequestBody SpuSaveDTO spuSaveDTO) {
//		skuInfoService.save(spuSaveVO);
        return null;
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("product:skuinfo:update")
    public R update(@RequestBody SkuInfoEntity skuInfo) {
        skuInfoService.updateById(skuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("product:skuinfo:delete")
    public R delete(@RequestBody Long[] skuIds) {
        skuInfoService.removeByIds(Arrays.asList(skuIds));

        return R.ok();
    }

}
