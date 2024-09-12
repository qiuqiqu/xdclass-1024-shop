package net.xdclass.service;


import net.xdclass.model.OrderMessage;
import net.xdclass.request.ConfirmOrderRequest;
import net.xdclass.util.JsonData;

public interface ProductOrderService {
    /**
     * 创建订单
     * @param orderRequest
     * @return
     */
    JsonData confirmOrder(ConfirmOrderRequest orderRequest);

    /**
     * 根据订单号查询 订单状态
     * @param outTradeNo
     * @return
     */
    String queryProductOrderState(String outTradeNo);

    /**
     * 队列监听，定时关单
     * @param orderMessage
     * @return
     */
    boolean closeProductOrder(OrderMessage orderMessage);
}
