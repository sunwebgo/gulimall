package com.xha.gulimall.order.web;

import com.xha.gulimall.order.dto.OrderSubmitDTO;
import com.xha.gulimall.order.service.OrderService;
import com.xha.gulimall.order.vo.OrderConfirmVO;
import com.xha.gulimall.order.vo.SubmitOrderResponseVO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Resource
    private OrderService orderService;

    /**
     * 确认页面显示
     *
     * @param model 模型
     * @return {@link String}
     * @throws ExecutionException   执行异常
     * @throws InterruptedException 中断异常
     */
    @GetMapping("/confirm.html")
    public String confirmPage(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVO orderConfirmVO = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", orderConfirmVO);
        return "confirm";
    }

    /**
     * 提交订单
     *
     * @param orderSubmitDTO 订单提交dto
     * @return {@link String}
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitDTO orderSubmitDTO, Model model, RedirectAttributes redirectAttributes){
        SubmitOrderResponseVO submitOrderResponseVO = orderService.submitOrder(orderSubmitDTO);
        if (!Objects.isNull(submitOrderResponseVO.getOrder())){
            model.addAttribute("submitOrderResp",submitOrderResponseVO);
            return "pay";
//           下单成功来到支付页面
        }else{
            redirectAttributes.addFlashAttribute("msg","下单失败");
            return "redirect:http://order.gulimall.com/confirm.html";
        }
    }
}
