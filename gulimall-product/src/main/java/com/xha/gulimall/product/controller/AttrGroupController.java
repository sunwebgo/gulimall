package com.xha.gulimall.product.controller;

import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.product.entity.AttrGroupEntity;
import com.xha.gulimall.product.service.AttrGroupService;
import com.xha.gulimall.product.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;


/**
 * 属性分组
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:39:19
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Resource
    private AttrGroupService attrGroupService;

    @Resource
    private CategoryService categoryService;

    /**
     * 分页获取分组属性分组
     */
    @RequestMapping("/list/{catelogId}")
//    @RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params, @PathVariable Long catelogId) {
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
//    @RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
//        1.根据分组id查询属性分组信息
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
//		  2.得到分类id
        Long catelogId = attrGroup.getCatelogId();
//        3.调用CategoryService，根据分类id查询分类id路径
        Long[] catelogPath = categoryService.findCatelogId(catelogId);
//        4.为实体类AttrGroupEntity添加CatelogPath属性
        attrGroup.setCatelogPath(catelogPath);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//    @RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
