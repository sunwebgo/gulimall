package com.xha.gulimall.ware.feign;

import com.xha.gulimall.common.to.ReceiveAddressTO;
import com.xha.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-member")
public interface MemberFeign {

    @RequestMapping("/member/memberreceiveaddress/info/{id}")
    public ReceiveAddressTO getReceiveAddress(@PathVariable("id") Long id);
}
