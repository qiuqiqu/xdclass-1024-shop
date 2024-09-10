package net.xdclass.mapper;

import net.xdclass.model.CouponRecordDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  优惠券记录 接口
 * </p>
 */
@Mapper
public interface CouponRecordMapper extends BaseMapper<CouponRecordDO> {

    /**
     * 批量更新优惠券使用记录
     * @param userId
     * @param useState
     * @param lockCouponRecordIds
     */
    int lockUseStateBatch(@Param("userId") Long userId,@Param("useState") String useState,@Param("lockCouponRecordIds") List<Long> lockCouponRecordIds);

    /**
     * 更新优惠券使用记录
     * @param couponRecordId
     * @param couponRecordId
     */
    void updateState(@Param("couponRecordId") Long couponRecordId, @Param("useState") String state);
}
