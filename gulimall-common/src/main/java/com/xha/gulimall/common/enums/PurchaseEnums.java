package com.xha.gulimall.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PurchaseEnums {

    NEM_BUILD(0,"新建"),
    DISTRIBUTE(1,"已分配"),
    BUYING(2,"已领取"),
    FINISH(3,"已完成"),
    HAVEEXCEPTION(4,"未完成");

    private int number;
    private String explain;
}
