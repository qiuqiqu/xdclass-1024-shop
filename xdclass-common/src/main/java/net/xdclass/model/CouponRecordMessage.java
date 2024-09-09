package net.xdclass.model;

import lombok.Data;

/**
 * MQ发送的消息
 */
@Data
public class CouponRecordMessage {


    /**
     * 消息id
     */
    private String messageId;

    /**
     * 订单号
     */
    private String outTradeNo;


    /**
     * 库存锁定任务id
     */
    private Long taskId;

}
