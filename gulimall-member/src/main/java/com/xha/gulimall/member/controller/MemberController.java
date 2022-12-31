package com.xha.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.xha.gulimall.member.feign.CouponFeignService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import com.xha.gulimall.member.entity.MemberEntity;
import com.xha.gulimall.member.service.MemberService;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.R;
import javax.annotation.Resource;


/**
 * 会员
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:44:37
 */
@RestController
@RefreshScope
@RequestMapping("member/member")
public class MemberController {
    @Resource
    private MemberService memberService;

    @Resource
    private CouponFeignService couponFeignService;

    @Value("${config.username}")
    private String username;


    @GetMapping("/coupons")
    public R test(){
        MemberEntity member = new MemberEntity();
        member.setUsername("zhangsan");
        Object coupons = couponFeignService.getMemberAllCoupon().get("coupons");
        return R.ok().put("member:",member).put("coupons",coupons);
    }

    @GetMapping("/config")
    public R test2(){
        return R.ok(username);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
//    @RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
//    @RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//    @RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
