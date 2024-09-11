package net.xdclass.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.enums.BizCodeEnum;
import net.xdclass.enums.CouponStateEnum;
import net.xdclass.exception.BizException;
import net.xdclass.feign.CouponFeignSerivce;
import net.xdclass.feign.ProductFeignService;
import net.xdclass.feign.UserFeignService;
import net.xdclass.interceptor.LoginInterceptor;
import net.xdclass.mapper.ProductOrderMapper;
import net.xdclass.model.LoginUser;
import net.xdclass.model.ProductOrderDO;
import net.xdclass.request.ConfirmOrderRequest;
import net.xdclass.request.LockCouponRecordRequest;
import net.xdclass.request.LockProductRequest;
import net.xdclass.request.OrderItemRequest;
import net.xdclass.service.ProductOrderService;
import net.xdclass.util.CommonUtil;
import net.xdclass.util.JsonData;
import net.xdclass.vo.CouponRecordVO;
import net.xdclass.vo.OrderItemVO;
import net.xdclass.vo.ProductOrderAddressVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ProductOrderServiceImpl implements ProductOrderService {
    @Autowired
    ProductOrderMapper productOrderMapper;

    @Autowired
    UserFeignService userFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    CouponFeignSerivce couponFeignSerivce;

    /**
     * * 防重提交
     * * 用户微服务-确认收货地址
     * * 商品微服务-获取最新购物项和价格
     * * 订单验价
     * * 优惠券微服务-获取优惠券
     * * 验证价格
     * * 锁定优惠券
     * * 锁定商品库存
     * * 创建订单对象
     * * 创建子订单对象
     * * 发送延迟消息-用于自动关单
     * * 创建支付信息-对接三方支付
     *
     * @param orderRequest
     * @return
     */
    @Override
    public JsonData confirmOrder(ConfirmOrderRequest orderRequest) {
        //获取当前登录用户
        ThreadLocal<LoginUser> threadLocal = LoginInterceptor.threadLocal;
        //生成订单号
        String orderOutTradeNo = CommonUtil.getStringNumRandom(32);
        //获取用户收货地址
        ProductOrderAddressVO addressVO = this.getUserAddress(orderRequest.getAddressId());
        log.info("收货地址信息:{}", addressVO);

        //获取用户加入购物车的商品
        List<Long> productIdList = orderRequest.getProductIdList();

        //获取购物车的最新商品价格（也会清空对应的购物车商品）
        JsonData cartItemDate = productFeignService.confirmOrderCartItem(productIdList);

        List<OrderItemVO> orderItemList  = cartItemDate.getData(new TypeReference<>(){});
        log.info("获取的商品:{}",orderItemList);
        if(orderItemList == null){
            //购物车商品不存在
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_CART_ITEM_NOT_EXIST);
        }

        //验证价格，减去商品优惠券
        this.checkPrice(orderItemList,orderRequest);

        //锁定优惠券
        this.lockCouponRecords(orderRequest ,orderOutTradeNo );

        //锁定库存
        this.lockProductStocks(orderItemList,orderOutTradeNo);


        //创建订单  TODO


        //创建支付  TODO
        return null;
    }

    /**
     * 锁定优惠券
     * @param orderRequest
     * @param orderOutTradeNo
     */
    private void lockCouponRecords(ConfirmOrderRequest orderRequest, String orderOutTradeNo) {
        List<Long> lockCouponRecordIds = new ArrayList<>();
        if(orderRequest.getCouponRecordId()>0){
            lockCouponRecordIds.add(orderRequest.getCouponRecordId());

            LockCouponRecordRequest lockCouponRecordRequest = new LockCouponRecordRequest();
            lockCouponRecordRequest.setOrderOutTradeNo(orderOutTradeNo);
            lockCouponRecordRequest.setLockCouponRecordIds(lockCouponRecordIds);

            //发起锁定优惠券请求
            JsonData jsonData = couponFeignSerivce.lockCouponRecords(lockCouponRecordRequest);
            if(jsonData.getCode()!=0){
                throw new BizException(BizCodeEnum.COUPON_RECORD_LOCK_FAIL);
            }
        }

    }


    /**
     * 锁定商品库存
     * @param orderItemList
     * @param orderOutTradeNo
     */
    private void lockProductStocks(List<OrderItemVO> orderItemList, String orderOutTradeNo) {

        List<OrderItemRequest> itemRequestList = orderItemList.stream().map(obj->{

            OrderItemRequest request = new OrderItemRequest();
            request.setBuyNum(obj.getBuyNum());
            request.setProductId(obj.getProductId());
            return request;
        }).collect(Collectors.toList());


        LockProductRequest lockProductRequest = new LockProductRequest();
        lockProductRequest.setOrderOutTradeNo(orderOutTradeNo);
        lockProductRequest.setOrderItemList(itemRequestList);

        JsonData jsonData = productFeignService.lockProductStock(lockProductRequest);
        if(jsonData.getCode()!=0){
            log.error("锁定商品库存失败：{}",lockProductRequest);
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_LOCK_PRODUCT_FAIL);
        }
    }

    /**
     * 验证价格
     * 1）统计全部商品的价格
     * 2) 获取优惠券(判断是否满足优惠券的条件)，总价再减去优惠券的价格 就是 最终的价格
     * @param orderItemList
     * @param orderRequest
     */
    private void checkPrice(List<OrderItemVO> orderItemList, ConfirmOrderRequest orderRequest) {
        //统计购买商品总价格
        BigDecimal realPayAmount = new BigDecimal("0");
        if (orderItemList != null) {
            for(OrderItemVO orderItemVO : orderItemList){
                BigDecimal itemRealPayAmount = orderItemVO.getTotalAmount();
                realPayAmount = realPayAmount.add(itemRealPayAmount);
            }
        }

        //获取优惠券，判断是否可以使用
        CouponRecordVO couponRecordVO = getCartCouponRecord(orderRequest.getCouponRecordId());

        //计算购物车价格，是否满足优惠券满减条件
        if(couponRecordVO!=null){

            //计算是否满足满减
            if(realPayAmount.compareTo(couponRecordVO.getConditionPrice()) < 0){
                throw new BizException(BizCodeEnum.ORDER_CONFIRM_COUPON_FAIL);
            }
            if(couponRecordVO.getPrice().compareTo(realPayAmount)>0){
                realPayAmount = BigDecimal.ZERO;

            }else {
                realPayAmount = realPayAmount.subtract(couponRecordVO.getPrice());
            }

        }

        // 前端传来的价格和后端的价格验价
        if(realPayAmount.compareTo(orderRequest.getRealPayAmount()) !=0 ){
            log.error("订单验价失败：{}",orderRequest);
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_PRICE_FAIL);
        }
    }

    /**
     * 获取优惠券
     * @param couponRecordId
     * @return
     */
    private CouponRecordVO getCartCouponRecord(Long couponRecordId) {

        if(couponRecordId ==null || couponRecordId < 0){
            return null;
        }

        JsonData couponData = couponFeignSerivce.findUserCouponRecordById(couponRecordId);

        if(couponData.getCode()!=0){
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_COUPON_FAIL);
        }

        if(couponData.getCode()==0){

            CouponRecordVO couponRecordVO = couponData.getData(new TypeReference<>(){});

            if(!couponAvailable(couponRecordVO)){
                log.error("优惠券使用失败");
                throw new BizException(BizCodeEnum.COUPON_UNAVAILABLE);
            }
            return couponRecordVO;
        }

        return null;
    }

    /**
     * 判断优惠券是否可用
     * @param couponRecordVO
     * @return
     */
    private boolean couponAvailable(CouponRecordVO couponRecordVO) {

        if(couponRecordVO.getUseState().equalsIgnoreCase(CouponStateEnum.NEW.name())){
            long currentTimestamp = CommonUtil.getCurrentTimestamp();
            long end = couponRecordVO.getEndTime().getTime();
            long start = couponRecordVO.getStartTime().getTime();
            if(currentTimestamp>= start && currentTimestamp<=end){
                return true;
            }
        }
        return false;
    }

    /**
     * 获取收货详情地址
     *
     * @param addressId
     * @return
     */
    private ProductOrderAddressVO getUserAddress(long addressId) {
        JsonData addressData = userFeignService.detail(addressId);
        if (addressData.getCode() != 0) {
            log.error("获取收获地址失败,msg:{}", addressData);
            throw new BizException(BizCodeEnum.ADDRESS_NO_EXITS);
        }

        ProductOrderAddressVO addressVO = addressData.getData(new TypeReference<>() {});

        return addressVO;
    }

    /**
     * 根据订单号查询 订单状态
     *
     * @param outTradeNo
     * @return
     */
    @Override
    public String queryProductOrderState(String outTradeNo) {
        ProductOrderDO productOrderDO = productOrderMapper.selectOne(new QueryWrapper<ProductOrderDO>().eq("out_trade_no", outTradeNo));
        if (productOrderDO == null) {
            return "";
        } else {
            return productOrderDO.getState();
        }
    }
}
