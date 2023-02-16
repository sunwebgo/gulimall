package com.xha.gulimall.seckill.controller;

import com.xha.gulimall.common.to.coupon.SeckillSkuRelationTO;
import com.xha.gulimall.common.utils.R;
import com.xha.gulimall.seckill.service.SeckillService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Controller
public class SeckillController {

    @Resource
    private SeckillService seckillService;

    /**
     * 获得秒杀的商品
     *
     * @return {@link R}
     */
    @ResponseBody
    @GetMapping("/getSeckillSkus")
    public R getSeckillSkus() {
        List<SeckillSkuRelationTO> SeckillSkuRelationTOList = seckillService.getSeckillSkus();
        return R.ok().setData(SeckillSkuRelationTOList);
    }

    /**
     * 根据skuID查询当前商品是否参加秒杀
     *
     * @return {@link R}
     */
    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getSeckillInfoBySkuId(@PathVariable("skuId") Long skuId) {
        SeckillSkuRelationTO seckillSkuRelationTO = seckillService.getSeckillInfoBySkuId(skuId);
        return R.ok().setData(seckillSkuRelationTO);
    }

    /**
     * 秒杀
     *
     * @param killId
     * @param key
     * @param num
     * kill?killId=1_3017598471&key=1225e9fe94fd47388a0545fc50592870&num=1
     * @return {@link R}
     */
    @GetMapping("/kill")
    public String seckill(@RequestParam("killId") String killId,
                          @RequestParam("key") String key,
                          @RequestParam("num") Integer num,
                          Model model) {
        String orderSn = seckillService.seckill(killId, num, key);
        model.addAttribute("orderSn",orderSn);
        return "success";
    }
}
