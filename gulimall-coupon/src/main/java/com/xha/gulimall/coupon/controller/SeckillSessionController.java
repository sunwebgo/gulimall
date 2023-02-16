package com.xha.gulimall.coupon.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.xha.gulimall.common.to.coupon.SeckillSessionTO;
import org.springframework.web.bind.annotation.*;

import com.xha.gulimall.coupon.entity.SeckillSessionEntity;
import com.xha.gulimall.coupon.service.SeckillSessionService;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.R;
import javax.annotation.Resource;


/**
 * 秒杀活动场次
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:43:17
 */
@RestController
@RequestMapping("coupon/seckillsession")
public class SeckillSessionController {
    @Resource
    private SeckillSessionService seckillSessionService;

    /**
     * 查询秒杀场次
     *
     * @return {@link List}<{@link SeckillSessionTO}>
     */
    @PostMapping("/getSeckillSession")
    public List<SeckillSessionTO> getSeckillSession(){
        return seckillSessionService.getSeckillSession();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
//    @RequiresPermissions("coupon:seckillsession:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = seckillSessionService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
//    @RequiresPermissions("coupon:seckillsession:info")
    public R info(@PathVariable("id") Long id){
		SeckillSessionEntity seckillSession = seckillSessionService.getById(id);

        return R.ok().put("seckillSession", seckillSession);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//    @RequiresPermissions("coupon:seckillsession:save")
    public R save(@RequestBody SeckillSessionEntity seckillSession){
		seckillSessionService.save(seckillSession);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("coupon:seckillsession:update")
    public R update(@RequestBody SeckillSessionEntity seckillSession){
		seckillSessionService.updateById(seckillSession);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("coupon:seckillsession:delete")
    public R delete(@RequestBody Long[] ids){
		seckillSessionService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
