package com.xha.gulimall.common.enums;

import lombok.Getter;

/**
 * 1.错误码定义规则为 5 为数字
 * <p>
 * 2. 前两位表示业务场景最后三位表示错误码
 * 3. 例如：100001。
 * * 10:通用
 * * 000:系统未知异常
 * 4. 维护错误码后需要维护错误描述，将他们定义为枚举形式
 * 5. 错误码列表：
 * * 10: 通用
 * *  001：参数格式校验
 * *  11: 商品
 * * 12: 订单
 * * 13: 购物车
 * * 14: 物流
 */

@Getter
public enum HttpCode {
    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败");

    private int code;
    private String message;

    HttpCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
