package net.xdclass.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.xdclass.model.CouponTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface CouponTaskMapper extends BaseMapper<CouponTaskDO> {

    /**
     * 批量插入
     * @param couponTaskDOList
     * @return
     */
    int insertBatch(@Param("couponTaskDOList") List<CouponTaskDO> couponTaskDOList);
}
