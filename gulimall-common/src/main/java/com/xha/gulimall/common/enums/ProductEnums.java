package com.xha.gulimall.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductEnums {
    ATTR_TYPE_BASE("基本属性", 1),
    ATTR_TYPE_SALE("销售属性",0),
    PUBLISH_STATUS_UP("下架",0),
    PUBLISH_STATUS_DROP("上架",1);

    private String comment;
    private Integer value;

}
