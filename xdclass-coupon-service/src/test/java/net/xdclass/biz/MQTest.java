package net.xdclass.biz;

import lombok.extern.slf4j.Slf4j;
import net.xdclass.CouponApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CouponApplication.class)
@Slf4j
public class MQTest {


    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void sendDelayMsg(){

        rabbitTemplate.convertAndSend("coupon.event.exchange","coupon.release.delay.routing.key","this is coupon record lock msg");
    }

}
