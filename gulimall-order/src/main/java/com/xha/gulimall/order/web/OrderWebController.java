package com.xha.gulimall.order.web;

import com.xha.gulimall.order.service.OrderService;
import com.xha.gulimall.order.vo.OrderConfirmVO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Resource
    private OrderService orderService;

    @GetMapping("/confirm.html")
    public String confirmPage(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVO orderConfirmVO = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", orderConfirmVO);
        return "confirm";
    }
}
