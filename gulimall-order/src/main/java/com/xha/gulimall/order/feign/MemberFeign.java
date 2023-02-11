package com.xha.gulimall.order.feign;

import com.xha.gulimall.common.to.member.ReceiveAddressTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("gulimall-member")
public interface MemberFeign {
    /**
     * 获得收获地址列表
     *
     * @param memberId 成员身份
     * @return {@link List}<{@link ReceiveAddressTO}>
     */
    @GetMapping("/member/memberreceiveaddress/address/{memberId}")
    public List<ReceiveAddressTO> getReceiveAddressList(@PathVariable("memberId") Long memberId);

    @RequestMapping("/member/memberreceiveaddress/info/{id}")
//    @RequiresPermissions("member:memberreceiveaddress:info")
    public ReceiveAddressTO getReceiveAddress(@PathVariable("id") Long id);
}
