package net.xdclass.feign;

import net.xdclass.util.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "xdclass-order-service")

public interface ProductOrderFeignSerivce {


    /**
     * 查询订单状态
     * @param outTradeNo
     * @return
     */
    @GetMapping("/api/order/v1/query_state")
    JsonData queryProductOrderState(@RequestParam("out_trade_no")String outTradeNo);


}
