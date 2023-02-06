package com.xha.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVO {
    private Long skuId;

    /**
     * 选中
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
     * 数量
     */
    private Integer count;

    /**
     * 总价格
     */
    private BigDecimal totalPrice;

    /**
     * 有货
     */
    private boolean hasStock;

    /**
     * 重量
     */
    private BigDecimal weight;
}
