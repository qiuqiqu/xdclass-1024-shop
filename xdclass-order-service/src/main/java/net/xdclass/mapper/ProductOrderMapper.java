package net.xdclass.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.xdclass.model.ProductOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductOrderMapper extends BaseMapper<ProductOrderDO> {

    /**
     * 更新订单状态
     * @param outTradeNo
     * @param newState
     * @param oldState
     */
    void updateOrderPayState(@Param("outTradeNo") String outTradeNo, @Param("newState") String newState, @Param("oldState") String oldState);
}
