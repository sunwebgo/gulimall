package com.xha.gulimall.product.controller;

import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.product.dto.spusavedto.SpuSaveDTO;
import com.xha.gulimall.product.entity.SpuInfoEntity;
import com.xha.gulimall.product.service.SpuInfoService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;


/**
 * spu信息
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:39:19
 */

@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {
    @Resource
    private SpuInfoService spuInfoService;

    /**
     * 根据条件查询spu信息
     */
    @RequestMapping("/list")
//    @RequiresPermissions("product:spuinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuInfoService.queryPageByConditation(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
//    @RequiresPermissions("product:spuinfo:info")
    public R info(@PathVariable("id") Long id){
		SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return R.ok().put("spuInfo", spuInfo);
    }

    /**
     * 保存商品信息
     *
     * @param spuSaveDTO spu拯救dto
     * @return {@link R}
     */
    @RequestMapping("/save")
//    @RequiresPermissions("product:skuinfo:save")
    public R saveSpuInfo(@RequestBody SpuSaveDTO spuSaveDTO){
//		skuInfoService.save(spuSaveVO);
        return spuInfoService.saveSpuInfo(spuSaveDTO);
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("product:spuinfo:update")
    public R update(@RequestBody SpuInfoEntity spuInfo){
		spuInfoService.updateById(spuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("product:spuinfo:delete")
    public R delete(@RequestBody Long[] ids){
		spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 上架商品
     *
     * @return {@link R}
     */
    @PostMapping("/up/{spuId}")
    public R upProduct(@PathVariable("spuId") Long spuId){
       return spuInfoService.upProduct(spuId);
    }


}
