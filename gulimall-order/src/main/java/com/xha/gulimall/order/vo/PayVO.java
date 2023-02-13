package com.xha.gulimall.order.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 支付数据VO
 *
 * @author Xu Huaiang
 * @date 2023/02/11
 */
@Data
@Accessors(chain = true)
public class PayVO {

    private String out_trade_no; // 商户订单号 必填
    private String subject; // 订单名称 必填
    private String total_amount;  // 付款金额 必填
    private String body; // 商品描述 可空
    private String timeout; //支付超时时间
}
