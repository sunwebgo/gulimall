package com.xha.gulimall.member.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.xha.gulimall.common.to.ReceiveAddressTO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import com.xha.gulimall.member.entity.MemberReceiveAddressEntity;
import com.xha.gulimall.member.service.MemberReceiveAddressService;
import com.xha.gulimall.common.utils.PageUtils;
import com.xha.gulimall.common.utils.R;
import javax.annotation.Resource;


/**
 * 会员收货地址
 *
 * @author XuHuaianag
 * @email 2533694604@qq.com
 * @date 2022-12-29 16:44:37
 */
@RestController
@RequestMapping("member/memberreceiveaddress")
public class MemberReceiveAddressController {
    @Resource
    private MemberReceiveAddressService memberReceiveAddressService;


    /**
     * 获得收获地址列表
     *
     * @param memberId 成员身份
     * @return {@link List}<{@link ReceiveAddressTO}>
     */
    @GetMapping("/address/{memberId}")
    public List<ReceiveAddressTO> getReceiveAddressList(@PathVariable("memberId") Long memberId){
        return memberReceiveAddressService.getReceiveAddressList(memberId);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
//    @RequiresPermissions("member:memberreceiveaddress:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberReceiveAddressService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 根据主键id获取到一条收货信息
     */
    @RequestMapping("/info/{id}")
//    @RequiresPermissions("member:memberreceiveaddress:info")
    public ReceiveAddressTO getReceiveAddress(@PathVariable("id") Long id){
        ReceiveAddressTO receiveAddressTO = new ReceiveAddressTO();
        MemberReceiveAddressEntity memberReceiveAddress = memberReceiveAddressService.getById(id);
        BeanUtils.copyProperties(memberReceiveAddress,receiveAddressTO);
        return receiveAddressTO;
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//    @RequiresPermissions("member:memberreceiveaddress:save")
    public R save(@RequestBody MemberReceiveAddressEntity memberReceiveAddress){
		memberReceiveAddressService.save(memberReceiveAddress);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("member:memberreceiveaddress:update")
    public R update(@RequestBody MemberReceiveAddressEntity memberReceiveAddress){
		memberReceiveAddressService.updateById(memberReceiveAddress);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("member:memberreceiveaddress:delete")
    public R delete(@RequestBody Long[] ids){
		memberReceiveAddressService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
