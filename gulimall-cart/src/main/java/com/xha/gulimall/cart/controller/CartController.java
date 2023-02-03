package com.xha.gulimall.cart.controller;

import com.xha.gulimall.cart.service.CartService;
import com.xha.gulimall.cart.vo.CartInfoVO;
import com.xha.gulimall.cart.vo.CartVO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;


@Controller
public class CartController {

    @Resource
    private CartService cartService;

    /**
     * 购物车列表页面
     * 浏览器有一个cookie; user-key; 标识用户身份，一个月后过期;
     * 如果第一次使用jd的购物车功能，都会给一个临时的用户身份;
     * 浏览器以后保存，每次访问都会带上这个cookie;
     * 登录: session有
     * 没登录:按照cookie里面带来user-key来做。
     * 第一次:如果没有临时用户，帮忙创建一个临时用户。
     *
     * @return {@link String}
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        CartVO cartVo = cartService.getCart();
        model.addAttribute("cart",cartVo);
        return "cart";
    }

    /**
     * 添加到购物车
     *
     * @return {@link String}
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") String skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId, num);
        redirectAttributes.addAttribute("skuId", skuId);
//        重定向到addToCartSuccess.html页,刷新不可再添加
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    /**
     * 查询当前成功添加到购物车的商品
     *
     * @return {@link String}
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") String skuId,Model model){
        CartInfoVO cartInfoVO = cartService.getCartItem(skuId);
        model.addAttribute("item",cartInfoVO);
        return "success";
    }


    /**
     * 更新选中状态
     *
     * @return {@link String}
     */
    @GetMapping("/checkItem")
    public String updateCheckStatus(@RequestParam("skuId") Long skuId,@RequestParam("check") Integer check){
        cartService.updateCheckStatus(skuId,check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }


    /**
     * 更新商品数量
     *
     * @param skuId sku id
     * @param num
     * @return {@link String}
     */
    @GetMapping("/countItem")
    public String updateNum(@RequestParam("skuId") Long skuId,@RequestParam("num") Integer num){
        cartService.updateNum(skuId,num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 删除商品
     *
     * @param skuId sku id
     * @return {@link String}
     */
    @GetMapping("/deleteItem")
    public String deleteProduct(@RequestParam("skuId") Long skuId){
        cartService.deleteProduct(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }
}
