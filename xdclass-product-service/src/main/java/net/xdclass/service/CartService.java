package net.xdclass.service;

import net.xdclass.request.CartItemRequest;
import net.xdclass.vo.CartVO;

public interface CartService {

    /**
     * 添加是商品到购物车
     * @param cartItemRequest
     */
    void addToCart(CartItemRequest cartItemRequest);

    /**
     * 清空购物车
     */
    void clear();

    /**
     * chak我的购物车
     * @return
     */
    CartVO getMyCart();
}
