package com.xha.gulimall.product.controller;

import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.product.dto.AttrDTO;
import com.xha.gulimall.product.service.AttrService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;


/**
 * 商品属性
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:39:19
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Resource
    private AttrService attrService;


    /**
     * 获取到属性列表
     *
     * @param params    参数个数
     * @param catelogId catelog id
     * @return {@link R}
     */
    @GetMapping("/{attrType}/list/{catelogId}")
    public R getAttrList(@RequestParam Map<String, Object> params, @PathVariable String attrType, @PathVariable Long catelogId) {
        PageUtils page = attrService.queryPage(params, attrType, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 根据attrId获得属性的详细信息
     */
    @RequestMapping("/info/{attrId}")
//    @RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId) {
        return attrService.getAttrDetailsInfo(attrId);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//    @RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrDTO attr) {
        attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改属性信息
     */
    @RequestMapping("/update")
//    @RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrDTO attr) {
        return attrService.updateAttr(attr);
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds) {
        return attrService.deleteAttr(attrIds);
    }


}
