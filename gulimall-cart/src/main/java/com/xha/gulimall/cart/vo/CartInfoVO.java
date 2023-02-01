package com.xha.gulimall.cart.vo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物项内容
 *
 * @author Xu Huaiang
 * @date 2023/02/01
 */
@Data
public class CartInfoVO {
    private Long skuId;

    /**
     * 检查
     */
    private Boolean check;

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
     * 价格
     */
    private BigDecimal price;

    /**
     * 数
     */
    private Integer count;

    /**
     * 总价格
     */
    private BigDecimal totalPrice;

//    public BigDecimal getTotalPrice() {
//        return this.price.multiply(new BigDecimal(this.count));
//    }

}
