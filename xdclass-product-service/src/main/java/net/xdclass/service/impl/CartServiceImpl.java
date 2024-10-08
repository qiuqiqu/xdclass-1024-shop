package net.xdclass.service.impl;

import com.alibaba.fastjson.JSON;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.constant.CacheKey;
import net.xdclass.enums.BizCodeEnum;
import net.xdclass.exception.BizException;
import net.xdclass.interceptor.LoginInterceptor;
import net.xdclass.model.LoginUser;
import net.xdclass.request.CartItemRequest;
import net.xdclass.service.CartService;
import net.xdclass.service.ProductService;
import net.xdclass.util.JsonData;
import net.xdclass.vo.CartItemVO;
import net.xdclass.vo.CartVO;
import net.xdclass.vo.ProductVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 **/

@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    private ProductService productService;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 添加商品到购物车(已 JSON 的格式保存)
     * @param cartItemRequest
     */
    @Override
    public void addToCart(CartItemRequest cartItemRequest) {

        long productId = cartItemRequest.getProductId();
        int buyNum = cartItemRequest.getBuyNum();

        //获取购物车
        BoundHashOperations<String,Object,Object> myCart =  getMyCartOps();

        //从购物车中获取商品
        Object cacheObj = myCart.get(productId);
        String result = "";

        if(cacheObj!=null){
           result =  (String)cacheObj;
        }

        if(StringUtils.isBlank(result)){
            //不存在则新建一个 购物项
            CartItemVO cartItemVO = new CartItemVO();

            ProductVO productVO = productService.findDetailById(productId);
            if(productVO == null){throw new BizException(BizCodeEnum.CART_FAIL);}

            cartItemVO.setAmount(productVO.getAmount());
            cartItemVO.setBuyNum(buyNum);
            cartItemVO.setProductId(productId);
            cartItemVO.setProductImg(productVO.getCoverImg());
            cartItemVO.setProductTitle(productVO.getTitle());
            myCart.put(productId,JSON.toJSONString(cartItemVO));

        }else {
            //存在商品，修改数量
            CartItemVO cartItem = JSON.parseObject(result,CartItemVO.class);
            cartItem.setBuyNum(cartItem.getBuyNum()+buyNum);
            myCart.put(productId,JSON.toJSONString(cartItem));
        }

    }

    /**
     * 删除购物项
     * @param productId
     */
    @Override
    public void deleteItem(long productId) {

        BoundHashOperations<String,Object,Object> mycart =  getMyCartOps();

        mycart.delete(productId);

    }

    /**
     * 清空购物车
     */
    @Override
    public void clear() {

        String cartKey = getCartKey();
        redisTemplate.delete(cartKey);

    }

    /**
     * 查看我的购物车
     * @return
     */
    @Override
    public CartVO getMyCart() {
        //获取购物车里全部购物项
        List<CartItemVO> cartItemVOList = buildCartItem(false);

        //封装成cartvo
        CartVO cartVO = new CartVO();
        cartVO.setCartItems(cartItemVOList);

        return cartVO;
    }

    /**
     * 修改购物车商品数量
     * @param cartItemRequest
     */
    @Override
    public void changeItemNum(CartItemRequest cartItemRequest) {
        BoundHashOperations<String,Object,Object> mycart =  getMyCartOps();

        Object cacheObj = mycart.get(cartItemRequest.getProductId());

        if(cacheObj==null){throw new BizException(BizCodeEnum.CART_FAIL);}

        String obj = (String)cacheObj;

        CartItemVO cartItemVO =  JSON.parseObject(obj,CartItemVO.class);
        cartItemVO.setBuyNum(cartItemRequest.getBuyNum());
        mycart.put(cartItemRequest.getProductId(),JSON.toJSONString(cartItemVO));
    }

    /**
     * 根据商品id 确认购物车商品信息
     * @param productIdList
     * @return
     */
    @Override
    public List<CartItemVO> confirmOrderCartItems(List<Long> productIdList) {
        //获取购物车的全部购物项 (同时获取的是最新价格)
        List<CartItemVO> cartItemVOList = buildCartItem(true);

        //根据需要的商品id进行过滤，并清空对应的购物项
        List<CartItemVO> resultList =  cartItemVOList.stream().filter(obj->{

            if(productIdList.contains(obj.getProductId())){
                this.deleteItem(obj.getProductId());
                return true;
            }
            return false;

        }).collect(Collectors.toList());

        return resultList;
    }

    /**
     * 获取购物车 全部 最新购物项
     * @param latestPrice 是否获取最新价格
     * @return
     */
    private List<CartItemVO> buildCartItem(boolean latestPrice) {
        BoundHashOperations<String,Object,Object> myCart = getMyCartOps();

        //获取购物车中所有的商品
        List<Object> itemList = myCart.values();

        //用于保存最终的购物车商 所有 商品列表
        List<CartItemVO> cartItemVOList = new ArrayList<>();

        //保存所有商品的 productId
        List<Long> productIdList = new ArrayList<>();

        for(Object item: itemList){
            CartItemVO cartItemVO = JSON.parseObject((String)item,CartItemVO.class);
            cartItemVOList.add(cartItemVO);

            productIdList.add(cartItemVO.getProductId());
        }

        //查询最新的商品价格
        if(latestPrice){
            setProductLatestPrice(cartItemVOList,productIdList);
        }

        return cartItemVOList;
    }

    /**
     * 设置商品最新价格
     * @param cartItemVOList
     * @param productIdList
     */
    private void setProductLatestPrice(List<CartItemVO> cartItemVOList, List<Long> productIdList) {

        //批量查询
        List<ProductVO> productVOList = productService.findProductsByIdBatch(productIdList);

        //分组
        Map<Long,ProductVO> maps = productVOList.stream().collect(Collectors.toMap(ProductVO::getId, Function.identity()));


        cartItemVOList.stream().forEach(item->{

            ProductVO productVO = maps.get(item.getProductId());
            item.setProductTitle(productVO.getTitle());
            item.setProductImg(productVO.getCoverImg());
            item.setAmount(productVO.getAmount());

        });


    }

    /**
     * * Map<String,Map<String,String>>  购物车 双层 map 结构
     * * 第一层Map，Key是用户id
     * * 第二层Map，Key是购物车中商品id，值是购物车数据
     * 抽取我的购物车，通用方法
     * @return
     */
    private BoundHashOperations<String,Object,Object> getMyCartOps(){
        String cartKey = getCartKey();
        return redisTemplate.boundHashOps(cartKey);
    }


    /**
     * 购物车 key
     * @return
     */
    private String getCartKey(){
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        String cartKey = String.format(CacheKey.CART_KEY,loginUser.getId());
        return cartKey;

    }


}
