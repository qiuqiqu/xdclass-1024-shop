package net.xdclass.service;

import net.xdclass.request.CartItemRequest;
import net.xdclass.vo.CartItemVO;
import net.xdclass.vo.CartVO;

import java.util.List;

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

    /**
     * 删除购物项
     * @param productId
     */
    void deleteItem(long productId);

    /**
     * 修改购物车商品数量
     * @param cartItemRequest
     */
    void changeItemNum(CartItemRequest cartItemRequest);

    /**
     * 确认购物车商品信息
     * @param productIdList
     * @return
     */
    List<CartItemVO> confirmOrderCartItems(List<Long> productIdList);
}
