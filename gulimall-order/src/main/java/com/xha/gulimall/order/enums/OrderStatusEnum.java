package com.xha.gulimall.order.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    CREATE_NEW(0,"待付款"),
    PAYED(1,"已付款"),
    SEND(2,"已发货"),
    ACHIEVED(3,"已完成"),
    CANCEL(4,"已取消"),
    SERVICING(5,"售后中"),
    SERVICED(6,"售后完成");


    private Integer code;
    private String status;
}
