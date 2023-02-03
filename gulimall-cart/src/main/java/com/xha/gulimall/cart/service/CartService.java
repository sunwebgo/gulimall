package com.xha.gulimall.cart.service;

import com.xha.gulimall.cart.vo.CartInfoVO;
import com.xha.gulimall.cart.vo.CartVO;

import java.util.concurrent.ExecutionException;

public interface CartService {
    CartInfoVO addToCart(String skuId, Integer num) throws ExecutionException, InterruptedException;

    CartInfoVO getCartItem(String skuId);

    CartVO getCart() throws ExecutionException, InterruptedException;

    void updateCheckStatus(Long skuId, Integer check);

    void updateNum(Long skuId, Integer num);

    void deleteProduct(Long skuId);
}
