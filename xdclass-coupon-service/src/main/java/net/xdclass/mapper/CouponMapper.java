package net.xdclass.mapper;

import net.xdclass.model.CouponDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 */
@Mapper
public interface CouponMapper extends BaseMapper<CouponDO> {

    /**
     * 优惠券库存扣减
     * @param couponId
     * @return
     */
    int reduceStock(@Param("couponId") long couponId);
}
