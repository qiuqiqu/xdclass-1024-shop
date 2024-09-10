package net.xdclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.mapper.ProductOrderMapper;
import net.xdclass.model.ProductOrderDO;
import net.xdclass.request.ConfirmOrderRequest;
import net.xdclass.service.ProductOrderService;
import net.xdclass.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ParameterMapper;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class ProductOrderServiceImpl implements ProductOrderService {
    @Autowired
    ProductOrderMapper productOrderMapper;

    /**
     * * 防重提交
     * * 用户微服务-确认收货地址
     * * 商品微服务-获取最新购物项和价格
     * * 订单验价
     *   * 优惠券微服务-获取优惠券
     *   * 验证价格
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
        return null;
    }

    /**
     * 根据订单号查询 订单状态
     * @param outTradeNo
     * @return
     */
    @Override
    public String queryProductOrderState(String outTradeNo) {
        ProductOrderDO productOrderDO = productOrderMapper.selectOne(new QueryWrapper<ProductOrderDO>().eq("out_trade_no", outTradeNo));
        if(productOrderDO == null){
            return "";
        }else {
            return productOrderDO.getState();
        }
    }
}
