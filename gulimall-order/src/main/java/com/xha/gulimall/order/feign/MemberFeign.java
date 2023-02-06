package com.xha.gulimall.order.feign;

import com.xha.gulimall.common.to.ReceiveAddressTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
}
