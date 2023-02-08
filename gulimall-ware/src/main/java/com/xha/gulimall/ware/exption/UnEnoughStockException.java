package com.xha.gulimall.ware.exption;

import lombok.Data;

@Data
public class UnEnoughStockException extends RuntimeException{

    private Long skuId;

    public UnEnoughStockException(Long skuId){
        super("当前商品:" + skuId + "库存不足!");
    }
}
