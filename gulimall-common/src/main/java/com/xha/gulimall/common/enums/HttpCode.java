package com.xha.gulimall.common.enums;

import lombok.AllArgsConstructor;
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
 * * 15: 用户
 * * 16: 数据
 */

@Getter
@AllArgsConstructor
public enum HttpCode {
    STATUS_NORMAL(200,"请求成功"),
    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    CHPRCHE_EXCEPTION(10002, "发送验证码频率过高，请稍后再试"),
    USER_EXIST_EXCEPTION(15001, "当前用户已存在"),
    PHONE_EXIST_EXCEPTION(15002, "当前手机号已经注册"),
    USER_NOT_EXIST_EXCEPTION(15003, "当前用户不存在"),
    PASSWORD_EXCEPTION(15004, "密码错误"),
    OAUTH_LOGIN_EXCEPTION(15005, "授权登录异常"),
    DATA_EXCEPTION(16001,"数据不能为空");

    private int code;
    private String message;


}
