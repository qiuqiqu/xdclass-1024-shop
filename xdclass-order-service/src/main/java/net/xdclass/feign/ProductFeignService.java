package net.xdclass.feign;

import net.xdclass.request.LockProductRequest;
import net.xdclass.util.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "xdclass-product-service")
public interface ProductFeignService {


    /**
     * 获取购物车的最新商品价格（也会清空对应的购物车商品）
     * @param productIdList
     * @return
     */
    @PostMapping("/api/cart/v1/confirm_order_cart_items")
    JsonData confirmOrderCartItem(@RequestBody List<Long> productIdList);

    /**
     * 锁定商品购物项库存
     * @param lockProductRequest
     * @return
     */
    @PostMapping("/api/product/v1/lock_products")
    JsonData lockProductStock(@RequestBody LockProductRequest lockProductRequest);


}
