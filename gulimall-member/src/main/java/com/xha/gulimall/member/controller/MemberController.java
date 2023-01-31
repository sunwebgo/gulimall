package com.xha.gulimall.member.controller;

import com.xha.gulimall.common.enums.HttpCode;
import com.xha.gulimall.common.to.GiteeResponseTO;
import com.xha.gulimall.common.to.UserLoginTO;
import com.xha.gulimall.common.to.UserRegisterTO;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.member.entity.MemberEntity;
import com.xha.gulimall.member.exception.PhoneExitException;
import com.xha.gulimall.member.exception.UsernameExitException;
import com.xha.gulimall.member.feign.CouponFeignService;
import com.xha.gulimall.member.service.MemberService;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;


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


    /**
     * 列表
     */
    @RequestMapping("/list")
//    @RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
//    @RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//    @RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 用户注册
     *
     * @return {@link R}
     */
    @PostMapping("/register")
    public R userRegister(@RequestBody UserRegisterTO userRegisterTO) {
        if (StringUtils.isEmpty(userRegisterTO.getUsername()) ||
                StringUtils.isEmpty(userRegisterTO.getPassword()) ||
                StringUtils.isEmpty(userRegisterTO.getPhone())) {
            return R.error(HttpCode.DATA_EXCEPTION.getCode(), HttpCode.DATA_EXCEPTION.getMessage());
        }
        try {
            memberService.userRegister(userRegisterTO);
        } catch (PhoneExitException e) {
            return R.error(HttpCode.PHONE_EXIST_EXCEPTION.getCode(),
                    HttpCode.PHONE_EXIST_EXCEPTION.getMessage());
        } catch (UsernameExitException e) {
            return R.error(HttpCode.USER_EXIST_EXCEPTION.getCode(),
                    HttpCode.USER_EXIST_EXCEPTION.getMessage());
        }
        return R.ok();
    }

    /**
     * 用户登录
     *
     * @param userLoginTO 用户登录
     * @return {@link R}
     */
    @PostMapping("/login")
    public R userLogin(@RequestBody UserLoginTO userLoginTO) {
        return memberService.userLogin(userLoginTO);
    }


    /**
     * Gitee第三方用户登录
     *
     * @return {@link R}
     */
    @PostMapping("/oauth/gitee/login")
    public R userOAuthGiteeLogin(@RequestBody GiteeResponseTO giteeResponseTO) {
        MemberEntity memberEntity = memberService.userOAuthGiteeLogin(giteeResponseTO);
        if (Objects.isNull(memberEntity)) {
            return R.error(HttpCode.OAUTH_LOGIN_EXCEPTION.getCode(),HttpCode.OAUTH_LOGIN_EXCEPTION.getMessage());
        }
        return R.ok().setData(memberEntity);
    }

}
