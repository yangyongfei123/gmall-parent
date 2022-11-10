package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

public interface CartApiService {
    void addToCart(Long skuId, String userId, Integer skuNum);

    List<CartInfo> getCartList(String userId, String userTempId);

    void checkCart(String userId, Integer isChecked, Long skuId);

    void deleteCart(String userId, Long skuId);

    List<CartInfo> getCartCheckedList(Long userId);
}
