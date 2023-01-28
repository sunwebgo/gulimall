package com.xha.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpuItemAttrGroupVO {
    /**
     * 属性分组名
     */
    private String groupName;

    /**
     * SpuBaseAttrVO对应的是属性名和属性值
     */
    private List<SpuBaseAttrVO> attrs;

}
