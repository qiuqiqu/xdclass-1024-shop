package net.xdclass.service;

import net.xdclass.vo.ProductVO;

import java.util.Map;

public interface ProductService {

    /**
     * 分页查询商品列表
     * @param page
     * @param size
     * @return
     */
    Map<String,Object> page(int page, int size);

    /**
     * 根据id找商品详情
     * @param productId
     * @return
     */
    ProductVO findDetailById(long productId);
}
