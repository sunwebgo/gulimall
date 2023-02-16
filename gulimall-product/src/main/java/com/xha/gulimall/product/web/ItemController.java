package com.xha.gulimall.product.web;

import com.xha.gulimall.product.service.SkuInfoService;
import com.xha.gulimall.product.vo.SkuItemVO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

@Controller
public class ItemController {

    @Resource
    private SkuInfoService skuInfoService;

    /**
     * 得到sku的详细信息
     *
     * @param skuId sku id
     * @return {@link String}
     */
    @GetMapping("/{skuId}.html")
    public String getSkuItemInfo(@PathVariable Long skuId, Model model) throws ExecutionException, InterruptedException {
        SkuItemVO skuItemVO = skuInfoService.getSkuItemInfo(skuId);
        model.addAttribute("item",skuItemVO);
        return "item";
    }
}
