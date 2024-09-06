package net.xdclass.service;

import net.xdclass.enums.CouponCategoryEnum;
import net.xdclass.request.NewUserCouponRequest;
import net.xdclass.util.JsonData;

import java.util.Map;

public interface CouponService {

    /**
     * 分页查询优惠券
     * @param page
     * @param size
     * @return
     */
    Map<String,Object> pageCouponActivity(int page, int size);

    /**
     * 领取优惠券接口
     * @param couponId
     * @param category
     * @return
     */
    JsonData addCoupon(long couponId, CouponCategoryEnum category);

    /**
     * 新用户注册发放优惠券
     * @param newUserCouponRequest
     * @return
     */
    JsonData initNewUserCoupon(NewUserCouponRequest newUserCouponRequest);
}
