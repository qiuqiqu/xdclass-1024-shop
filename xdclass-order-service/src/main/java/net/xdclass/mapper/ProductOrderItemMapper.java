package net.xdclass.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.xdclass.model.ProductOrderItemDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductOrderItemMapper extends BaseMapper<ProductOrderItemDO> {

    /**
     * 批量插入
     * @param list
     */
    void insertBatch( @Param("orderItemList") List<ProductOrderItemDO> list);
}
