package net.xdclass.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.model.CouponRecordMessage;
import net.xdclass.service.CouponRecordService;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 监听死信队列 相当消费者 (类似 12 序号的那个 "锁定恢复服务" 线路)
 */
@Slf4j
@Component
@RabbitListener(queues = "${mqconfig.coupon_release_queue}")
public class CouponMQListener {


    @Autowired
    private CouponRecordService couponRecordService;

    @Autowired
    private RedissonClient redissonClient;

    /**
     *
     * 重复消费-幂等性
     *
     * 消费失败，重新入队后最大重试次数：
     *  如果消费失败，不重新入队，可以记录日志，然后插到数据库人工排查
     *
     *  消费者这块还有啥问题，大家可以先想下，然后给出解决方案
     *
     * @param recordMessage
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    public void releaseCouponRecord(CouponRecordMessage recordMessage, Message message, Channel channel) throws IOException {

        log.info("监听到消息：releaseCouponRecord消息内容：{}", recordMessage);
        long msgTag = message.getMessageProperties().getDeliveryTag();

        boolean flag = couponRecordService.releaseCouponRecord(recordMessage);

        //防止同个解锁任务并发进入；如果是串行消费不用加锁；加锁有利也有弊，看项目业务逻辑而定
        //Lock lock = redissonClient.getLock("lock:coupon_record_release:"+recordMessage.getTaskId());
        //lock.lock();
        try {
            if (flag) {
                //确认消息消费成功
                channel.basicAck(msgTag, false);
            }else {
                log.error("释放优惠券失败 flag=false,{}",recordMessage);
                //消息重新入队
                channel.basicReject(msgTag,true);
            }

        } catch (IOException e) {
            log.error("释放优惠券记录异常:{},msg:{}",e,recordMessage);
            //消息重新入队
            channel.basicReject(msgTag,true);
        }
//        finally {
//            lock.unlock();
//        }


    }


}
