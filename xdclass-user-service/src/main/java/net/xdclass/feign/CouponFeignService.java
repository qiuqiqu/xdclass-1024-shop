package net.xdclass.feign;

import net.xdclass.request.NewUserCouponRequest;
import net.xdclass.util.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(value = "xdclass-coupon-service")
public interface CouponFeignService {

    /**
     * 新用户注册发放优惠券
     *
     * @param newUserCouponRequest
     * @return
     */
    @PostMapping("/api/coupon/v1/new_user_coupon")
    JsonData addNewUserCoupon(@RequestBody NewUserCouponRequest newUserCouponRequest);

}
