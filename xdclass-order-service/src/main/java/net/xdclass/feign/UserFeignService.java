package net.xdclass.feign;

import net.xdclass.util.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "xdclass-user-service")
public interface UserFeignService {


    /**
     * 查询用户地址，接口本身有防止水平权限
     * @param addressId
     * @return
     */
    @GetMapping("/api/address/v1//find/{address_id}")
    JsonData detail(@PathVariable("address_id")long addressId);

}
