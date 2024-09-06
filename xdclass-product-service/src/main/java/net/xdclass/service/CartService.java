package net.xdclass.service;

import net.xdclass.request.CartItemRequest;

public interface CartService {

    /**
     * 添加是商品到购物车
     * @param cartItemRequest
     */
    void addToCart(CartItemRequest cartItemRequest);
}
