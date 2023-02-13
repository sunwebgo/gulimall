package com.xha.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.xha.gulimall.order.config.AlipayTemplate;
import com.xha.gulimall.order.service.OrderService;
import com.xha.gulimall.order.vo.AliPayAsyncNotifyVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
public class OrderPayedListener {

    @Resource
    private OrderService orderService;

    @Resource
    private AlipayTemplate alipayTemplate;

    /**
     * 支付异步通知
     * 对于 PC 网站支付的交易，在用户支付完成之后，
     * 支付宝会根据 API 中商家传入的 notify_url，
     * 通过 POST 请求的形式将支付结果作为参数通知到商家系统。
     *
     * @param request 请求
     * @return {@link String}
     */
    @PostMapping("/payed/notify/test")
    public String handleAlipayed(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (String key : parameterMap.keySet()) {
            String value = request.getParameter(key);
            System.out.println("参数名：" + key + ",参数值：" + value);
        }
        return "success";
    }

    /**
     * 处理支付异步通知
     * 对于 PC 网站支付的交易，在用户支付完成之后，
     * 支付宝会根据 API 中商家传入的 notify_url，
     * 通过 POST 请求的形式将支付结果作为参数通知到商家系统。
     *
     * @return {@link String}
     */
    @PostMapping("/payed/notify")
    public String handleAliPayAsyncNotifyResponse(AliPayAsyncNotifyVO aliPayAsyncNotifyVO,
                                                  HttpServletRequest request) throws AlipayApiException {
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        boolean signVerified = AlipaySignature.rsaCheckV1(params,
                alipayTemplate.getAlipay_public_key(),
                alipayTemplate.getCharset(),
                alipayTemplate.getSign_type()); //调用SDK验证签名
        if (signVerified) {
//             验证签名成功
            System.out.println("签名验证成功");
            return orderService.handleAliPayAsyncNotifyResponse(aliPayAsyncNotifyVO);
        } else {
            System.out.println("签名验证失败");
            return "error";
        }
    }
}
