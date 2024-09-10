package net.xdclass.model;

import lombok.Data;

@Data
public class ProductMessage {

    /**
     * 消息队列id
     */
    private Long messageId;


    /**
     * 订单号
     */
    private String outTradeNo;

    /**
     * 库存锁定工作单id
     */
    private Long taskId;

}