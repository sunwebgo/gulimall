package com.xha.gulimall.cart.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * 整个购物车
 *
 * @author Xu Huaiang
 * @date 2023/02/01
 */
@Data
@Accessors(chain = true)
public class CartVO {

    /**
     * 购物项列表
     */
    private List<CartInfoVO> items;

    /**
     * 商品数量
     */
    private Integer productNum;

    /**
     * 商品类型数量
     */
    private Integer productTypeNum;

    /**
     * 购物车所有商品价格总和
     */
    private BigDecimal totalAmountPrice;

    /**
     * 优惠价格
     */
    private BigDecimal reducePrice = new BigDecimal(0);
}
