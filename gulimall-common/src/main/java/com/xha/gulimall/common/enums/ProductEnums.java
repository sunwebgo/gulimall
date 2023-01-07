package com.xha.gulimall.common.enums;

import lombok.Getter;

@Getter
public enum ProductEnums {
    ATTR_TYPE_BASE("基本属性", 1),
    ATTR_TYPE_SALE("销售属性",0);

    private String comment;
    private Integer value;

    ProductEnums(String comment, Integer value) {
        this.comment = comment;
        this.value = value;
    }
}
