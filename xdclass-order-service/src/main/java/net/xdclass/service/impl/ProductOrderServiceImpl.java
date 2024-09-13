package net.xdclass.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.component.PayFactory;
import net.xdclass.config.RabbitMQConfig;
import net.xdclass.constant.TimeConstant;
import net.xdclass.enums.*;
import net.xdclass.exception.BizException;
import net.xdclass.feign.CouponFeignSerivce;
import net.xdclass.feign.ProductFeignService;
import net.xdclass.feign.UserFeignService;
import net.xdclass.interceptor.LoginInterceptor;
import net.xdclass.mapper.ProductOrderItemMapper;
import net.xdclass.mapper.ProductOrderMapper;
import net.xdclass.model.LoginUser;
import net.xdclass.model.OrderMessage;
import net.xdclass.model.ProductOrderDO;
import net.xdclass.model.ProductOrderItemDO;
import net.xdclass.request.ConfirmOrderRequest;
import net.xdclass.request.LockCouponRecordRequest;
import net.xdclass.request.LockProductRequest;
import net.xdclass.request.OrderItemRequest;
import net.xdclass.service.ProductOrderService;
import net.xdclass.util.CommonUtil;
import net.xdclass.util.JsonData;
import net.xdclass.vo.CouponRecordVO;
import net.xdclass.vo.OrderItemVO;
import net.xdclass.vo.PayInfoVO;
import net.xdclass.vo.ProductOrderAddressVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ProductOrderServiceImpl<rabbitTemplate> implements ProductOrderService {
    @Autowired
    ProductOrderMapper productOrderMapper;

    @Autowired
    UserFeignService userFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    CouponFeignSerivce couponFeignSerivce;

    @Autowired
    private ProductOrderItemMapper productOrderItemMapper;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitMQConfig rabbitConfig;
    
    @Autowired
    PayFactory payFactory;

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
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        //生成订单号
        String orderOutTradeNo = CommonUtil.getStringNumRandom(32);
        //获取用户收货地址
        ProductOrderAddressVO addressVO = this.getUserAddress(orderRequest.getAddressId());
        log.info("收货地址信息:{}", addressVO);

        //获取用户加入购物车的商品
        List<Long> productIdList = orderRequest.getProductIdList();

        //获取购物车的最新商品价格（也会清空对应的购物车商品）
        JsonData cartItemDate = productFeignService.confirmOrderCartItem(productIdList);

        List<OrderItemVO> orderItemList = cartItemDate.getData(new TypeReference<>() {
        });
        log.info("获取的商品:{}", orderItemList);
        if (orderItemList == null) {
            //购物车商品不存在
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_CART_ITEM_NOT_EXIST);
        }

        //验证价格，减去商品优惠券
        this.checkPrice(orderItemList, orderRequest);

        //锁定优惠券
        this.lockCouponRecords(orderRequest, orderOutTradeNo);

        //锁定库存
        this.lockProductStocks(orderItemList, orderOutTradeNo);


        //创建订单
        ProductOrderDO productOrderDO = this.saveProductOrder(orderRequest, loginUser, orderOutTradeNo, addressVO);

        //创建订单项
        this.saveProductOrderItems(orderOutTradeNo, productOrderDO.getId(), orderItemList);

        //发送延迟消息，用于自动关单
        OrderMessage orderMessage = new OrderMessage();
        orderMessage.setOutTradeNo(orderOutTradeNo);
        rabbitTemplate.convertAndSend(rabbitConfig.getEventExchange(), rabbitConfig.getOrderCloseDelayRoutingKey(), orderMessage);

        //创建支付
        PayInfoVO payInfoVO = new PayInfoVO();
        //订单号
        payInfoVO.setOutTradeNo(orderOutTradeNo);
        //订单总金额
        payInfoVO.setPayFee(productOrderDO.getPayAmount());
        //支付类型 微信-支付宝-银行-其他
        payInfoVO.setPayType(orderRequest.getPayType());
        //端类型 APP/H5/PC
        payInfoVO.setClientType(orderRequest.getClientType());
        //标题
        payInfoVO.setTitle(orderItemList.get(0).getProductTitle());
        //描述
        payInfoVO.setDescription("");
        //订单支付超时时间，毫秒   30分
        payInfoVO.setOrderPayTimeoutMills(TimeConstant.ORDER_PAY_TIMEOUT_MILLS);

        String payResult = payFactory.pay(payInfoVO);
        if(StringUtils.isNotBlank(payResult)){
            log.info("创建支付订单成功:payInfoVO={},payResult={}",payInfoVO,payResult);
            return JsonData.buildSuccess(payResult);
        }else {
            log.error("创建支付订单失败:payInfoVO={},payResult={}",payInfoVO,payResult);
            return JsonData.buildResult(BizCodeEnum.PAY_ORDER_FAIL);
        }

    }

    /**
     * 新增订单项
     *
     * @param orderOutTradeNo
     * @param orderId
     * @param orderItemList
     */
    private void saveProductOrderItems(String orderOutTradeNo, Long orderId, List<OrderItemVO> orderItemList) {
        List<ProductOrderItemDO> list = orderItemList.stream().map(obj -> {
            ProductOrderItemDO itemDO = new ProductOrderItemDO();
            itemDO.setBuyNum(obj.getBuyNum());
            itemDO.setProductId(obj.getProductId());
            itemDO.setProductImg(obj.getProductImg());
            itemDO.setProductName(obj.getProductTitle());

            itemDO.setOutTradeNo(orderOutTradeNo);
            itemDO.setCreateTime(new Date());

            //单价
            itemDO.setAmount(obj.getAmount());
            //总价
            itemDO.setTotalAmount(obj.getTotalAmount());
            itemDO.setProductOrderId(orderId);
            return itemDO;

        }).collect(Collectors.toList());

        productOrderItemMapper.insertBatch(list);
    }


    /**
     * 创建订单
     *
     * @param orderRequest
     * @param loginUser
     * @param orderOutTradeNo
     * @param addressVO
     */
    private ProductOrderDO saveProductOrder(ConfirmOrderRequest orderRequest, LoginUser loginUser, String orderOutTradeNo, ProductOrderAddressVO addressVO) {

        ProductOrderDO productOrderDO = new ProductOrderDO();
        productOrderDO.setUserId(loginUser.getId());
        productOrderDO.setHeadImg(loginUser.getHeadImg());
        productOrderDO.setNickname(loginUser.getName());

        productOrderDO.setOutTradeNo(orderOutTradeNo);
        productOrderDO.setCreateTime(new Date());
        productOrderDO.setDel(0);
        productOrderDO.setOrderType(ProductOrderTypeEnum.DAILY.name());

        //实际支付的价格
        productOrderDO.setPayAmount(orderRequest.getRealPayAmount());

        //总价，未使用优惠券的价格
        productOrderDO.setTotalAmount(orderRequest.getTotalAmount());
        productOrderDO.setState(ProductOrderStateEnum.NEW.name());
        ProductOrderTypeEnum.valueOf(orderRequest.getPayType()).name();

        productOrderDO.setReceiverAddress(JSON.toJSONString(addressVO));

        productOrderMapper.insert(productOrderDO);

        return productOrderDO;

    }

    /**
     * 锁定优惠券
     *
     * @param orderRequest
     * @param orderOutTradeNo
     */
    private void lockCouponRecords(ConfirmOrderRequest orderRequest, String orderOutTradeNo) {
        List<Long> lockCouponRecordIds = new ArrayList<>();
        if (orderRequest.getCouponRecordId() > 0) {
            lockCouponRecordIds.add(orderRequest.getCouponRecordId());

            LockCouponRecordRequest lockCouponRecordRequest = new LockCouponRecordRequest();
            lockCouponRecordRequest.setOrderOutTradeNo(orderOutTradeNo);
            lockCouponRecordRequest.setLockCouponRecordIds(lockCouponRecordIds);

            //发起锁定优惠券请求
            JsonData jsonData = couponFeignSerivce.lockCouponRecords(lockCouponRecordRequest);
            if (jsonData.getCode() != 0) {
                throw new BizException(BizCodeEnum.COUPON_RECORD_LOCK_FAIL);
            }
        }

    }


    /**
     * 锁定商品库存
     *
     * @param orderItemList
     * @param orderOutTradeNo
     */
    private void lockProductStocks(List<OrderItemVO> orderItemList, String orderOutTradeNo) {

        List<OrderItemRequest> itemRequestList = orderItemList.stream().map(obj -> {

            OrderItemRequest request = new OrderItemRequest();
            request.setBuyNum(obj.getBuyNum());
            request.setProductId(obj.getProductId());
            return request;
        }).collect(Collectors.toList());


        LockProductRequest lockProductRequest = new LockProductRequest();
        lockProductRequest.setOrderOutTradeNo(orderOutTradeNo);
        lockProductRequest.setOrderItemList(itemRequestList);

        JsonData jsonData = productFeignService.lockProductStock(lockProductRequest);
        if (jsonData.getCode() != 0) {
            log.error("锁定商品库存失败：{}", lockProductRequest);
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_LOCK_PRODUCT_FAIL);
        }
    }

    /**
     * 验证价格
     * 1）统计全部商品的价格
     * 2) 获取优惠券(判断是否满足优惠券的条件)，总价再减去优惠券的价格 就是 最终的价格
     *
     * @param orderItemList
     * @param orderRequest
     */
    private void checkPrice(List<OrderItemVO> orderItemList, ConfirmOrderRequest orderRequest) {
        //统计购买商品总价格
        BigDecimal realPayAmount = new BigDecimal("0");
        if (orderItemList != null) {
            for (OrderItemVO orderItemVO : orderItemList) {
                BigDecimal itemRealPayAmount = orderItemVO.getTotalAmount();
                realPayAmount = realPayAmount.add(itemRealPayAmount);
            }
        }

        //获取优惠券，判断是否可以使用
        CouponRecordVO couponRecordVO = getCartCouponRecord(orderRequest.getCouponRecordId());

        //计算购物车价格，是否满足优惠券满减条件
        if (couponRecordVO != null) {

            //计算是否满足满减
            if (realPayAmount.compareTo(couponRecordVO.getConditionPrice()) < 0) {
                throw new BizException(BizCodeEnum.ORDER_CONFIRM_COUPON_FAIL);
            }
            if (couponRecordVO.getPrice().compareTo(realPayAmount) > 0) {
                realPayAmount = BigDecimal.ZERO;

            } else {
                realPayAmount = realPayAmount.subtract(couponRecordVO.getPrice());
            }

        }

        // 前端传来的价格和后端的价格验价
        if (realPayAmount.compareTo(orderRequest.getRealPayAmount()) != 0) {
            log.error("订单验价失败：{}", orderRequest);
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_PRICE_FAIL);
        }
    }

    /**
     * 获取优惠券
     *
     * @param couponRecordId
     * @return
     */
    private CouponRecordVO getCartCouponRecord(Long couponRecordId) {

        if (couponRecordId == null || couponRecordId < 0) {
            return null;
        }

        JsonData couponData = couponFeignSerivce.findUserCouponRecordById(couponRecordId);

        if (couponData.getCode() != 0) {
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_COUPON_FAIL);
        }

        if (couponData.getCode() == 0) {

            CouponRecordVO couponRecordVO = couponData.getData(new TypeReference<>() {
            });

            if (!couponAvailable(couponRecordVO)) {
                log.error("优惠券使用失败");
                throw new BizException(BizCodeEnum.COUPON_UNAVAILABLE);
            }
            return couponRecordVO;
        }

        return null;
    }

    /**
     * 判断优惠券是否可用
     *
     * @param couponRecordVO
     * @return
     */
    private boolean couponAvailable(CouponRecordVO couponRecordVO) {

        if (couponRecordVO.getUseState().equalsIgnoreCase(CouponStateEnum.NEW.name())) {
            long currentTimestamp = CommonUtil.getCurrentTimestamp();
            long end = couponRecordVO.getEndTime().getTime();
            long start = couponRecordVO.getStartTime().getTime();
            if (currentTimestamp >= start && currentTimestamp <= end) {
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

        ProductOrderAddressVO addressVO = addressData.getData(new TypeReference<>() {
        });

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

    /**
     * 定时关单
     *
     * @param orderMessage
     * @return
     */
    @Override
    public boolean closeProductOrder(OrderMessage orderMessage) {
        //查询当前订单
        ProductOrderDO productOrderDO = productOrderMapper.selectOne(new QueryWrapper<ProductOrderDO>().eq("out_trade_no", orderMessage.getOutTradeNo()));
        if (productOrderDO == null) { //订单不存在
            log.warn("直接确认消息，订单不存在:{}", orderMessage);
            return true;
        }
        if (productOrderDO.getState().equalsIgnoreCase(ProductOrderStateEnum.PAY.name())) {  //已经支付
            log.info("直接确认消息,订单已经支付:{}", orderMessage);
            return true;
        }


        //本地取消订单前,向第三方支付查询订单是否真的未支付
        PayInfoVO payInfoVO = new PayInfoVO();
        payInfoVO.setPayType(productOrderDO.getPayType());
        payInfoVO.setOutTradeNo(productOrderDO.getOutTradeNo());
        String payResult  = payFactory.queryPaySuccess(payInfoVO);;

        //结果为空，则未支付成功，本地取消订单(修改订单状态)
        if(StringUtils.isBlank(payResult)){
            productOrderMapper.updateOrderPayState(productOrderDO.getOutTradeNo(),ProductOrderStateEnum.CANCEL.name(),ProductOrderStateEnum.NEW.name());
            log.info("结果为空，则未支付成功，本地取消订单:{}",orderMessage);
            return true;
        }else {
            //支付成功，主动的把订单状态改成UI就支付，造成该原因的情况可能是支付通道回调有问题
            log.warn("支付成功，主动的把订单状态改成UI就支付，造成该原因的情况可能是支付通道回调有问题:{}",orderMessage);
            productOrderMapper.updateOrderPayState(productOrderDO.getOutTradeNo(),ProductOrderStateEnum.PAY.name(),ProductOrderStateEnum.NEW.name());
            return true;
        }

    }

    /**
     * 支付结果回更新订单状态
     * @param payType
     * @param paramsMap
     * @return
     */
    @Override
    public JsonData handlerOrderCallbackMsg(ProductOrderPayTypeEnum payType, Map<String, String> paramsMap) {
        if(payType.name().equalsIgnoreCase(ProductOrderPayTypeEnum.ALIPAY.name())){
            //支付宝支付
            //获取商户订单号
            String outTradeNo = paramsMap.get("out_trade_no");
            //交易的状态
            String tradeStatus = paramsMap.get("trade_status");

            if("TRADE_SUCCESS".equalsIgnoreCase(tradeStatus) || "TRADE_FINISHED".equalsIgnoreCase(tradeStatus)){
                //更新订单状态
                productOrderMapper.updateOrderPayState(outTradeNo,ProductOrderStateEnum.PAY.name(),ProductOrderStateEnum.NEW.name());
                return JsonData.buildSuccess();
            }

        } else if(payType.name().equalsIgnoreCase(ProductOrderPayTypeEnum.WECHAT.name())){
            //微信支付  TODO
        }

        return JsonData.buildResult(BizCodeEnum.PAY_ORDER_CALLBACK_NOT_SUCCESS);
    }
}
