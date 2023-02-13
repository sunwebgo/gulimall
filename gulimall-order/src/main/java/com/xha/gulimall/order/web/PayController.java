package com.xha.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.xha.gulimall.common.to.order.OrderTO;
import com.xha.gulimall.order.config.AlipayTemplate;
import com.xha.gulimall.order.entity.OrderItemEntity;
import com.xha.gulimall.order.service.OrderItemService;
import com.xha.gulimall.order.service.OrderService;
import com.xha.gulimall.order.vo.PayVO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Controller
public class PayController {

    @Resource
    private AlipayTemplate alipayTemplate;

    @Resource
    private OrderService orderService;

    @Resource
    private OrderItemService orderItemService;

    @ResponseBody
    @GetMapping(value = "/aliPayOrder",produces = MediaType.TEXT_HTML_VALUE)
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
//        1.根据订单号获取到当前订单信息
        OrderTO order = orderService.getOrderById(orderSn);
//        2.查询订单项信息
        OrderItemEntity orderItem = orderItemService.getOrderItemById(orderSn);
        PayVO payVO = new PayVO();
        payVO.setOut_trade_no(orderSn)
                .setTotal_amount(order.getPayAmount().setScale(2, BigDecimal.ROUND_UP).toString())
                .setSubject(orderItem.getSkuName())
                .setBody(orderItem.getSkuAttrsVals())
                .setTimeout("1m");
        String pay = alipayTemplate.pay(payVO);
        return pay;
    }
}
