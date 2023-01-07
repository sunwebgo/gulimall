package com.xha.gulimall.product.controller;

import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.product.dto.AttrGroupDTO;
import com.xha.gulimall.product.entity.AttrGroupEntity;
import com.xha.gulimall.product.service.AttrGroupService;
import com.xha.gulimall.product.service.CategoryService;
import com.xha.gulimall.product.vo.AttrGroupVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
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
     * 根据属性分组id分页获取分组属性分组
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
//        4.将AttrGroupEntity对象转换为AttrVO对象
        AttrGroupVO attrGroupVO = new AttrGroupVO();
        BeanUtils.copyProperties(attrGroup, attrGroupVO);
//        4.为实体类AttrGroupEntity添加CatelogPath属性

        attrGroupVO.setCatelogPath(catelogPath);
        return R.ok().put("attrGroup", attrGroupVO);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//    @RequiresPermissions("product:attrgroup:save")
    public R save(@Valid @RequestBody AttrGroupEntity attrGroup) {
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
        return attrGroupService.deleteAttrGroups(attrGroupIds);
    }

    /**
     * 查询属性-属性分组的关联关系
     *
     * @param attrGroupId attr组id
     * @return {@link R}
     */
    @GetMapping("/attr/relation/{attrGroupId}")
    public R getAttrGroupRelation(@PathVariable Long attrGroupId) {
        return attrGroupService.getAttrGroupRelation(attrGroupId);
    }

    /**
     * 删除属性分组和属性的关联关系
     *
     * @param attrGroupDTO attr集团dto
     * @return {@link R}
     */
    @RequestMapping("/attr/relation/delete")
    public R removeAttrGroupRelation(@RequestBody AttrGroupDTO[] attrGroupDTO) {
        return attrGroupService.removeAttrGroupRelation(attrGroupDTO);
    }

    /**
     * 获取属性分组没有关联的其他属性
     *
     * @return {@link R}
     */
    @RequestMapping("/noattr/relation/{attrGroupId}")
    public R getAttrGroupNoRelation(@PathVariable Long attrGroupId, @RequestParam Map<String, Object> params) {
        PageUtils page = attrGroupService.getAttrGroupNoRelation(attrGroupId,params);
        return R.ok().put("page",page);
    }

    /**
     * 添加属性分组和属性的关联关系
     *
     * @param attrGroupDTO attr集团dto
     * @return {@link R}
     */
    @RequestMapping("/attr/relation")
    public R addAttrAndAttrGroupRelation(@RequestBody AttrGroupDTO[] attrGroupDTO){
        return attrGroupService.addAttrAndAttrGroupRelation(attrGroupDTO);
    }


    /**
     * 获取分类下的属性分组和属性
     *
     * @param catelogId catelog id
     * @return {@link R}
     */
    @GetMapping("/withattr/{catelogId}")
    public R getCategoryAttrGroup(@PathVariable Long catelogId){
        return attrGroupService.getCategoryAttrGroup(catelogId);
    }

}
