package com.xha.gulimall.common.to.cart;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物项内容
 *
 * @author Xu Huaiang
 * @date 2023/02/01
 */
@Data
@Accessors(chain = true)
public class CartInfoTO {
    private Long skuId;

    /**
     * 检查
     */
    private Boolean check = true;

    /**
     * 标题
     */
    private String title;

    /**
     * 图像
     */
    private String image;

    /**
     * sku attr
     */
    private List<String> skuAttr;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 数量
     */
    private Integer count;

    /**
     * 总价格
     */
    private BigDecimal totalPrice;

}
