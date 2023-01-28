package com.xha.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SpuBaseAttrVO {
    private String attrName;

    private String attrValue;
}
