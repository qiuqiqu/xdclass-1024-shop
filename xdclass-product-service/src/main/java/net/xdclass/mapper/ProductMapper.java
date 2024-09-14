package net.xdclass.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.xdclass.model.ProductDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 *
 */
@Mapper
public interface ProductMapper extends BaseMapper<ProductDO> {
    /**
     * 锁定商品库存
     *
     * @param productId
     * @param buyNum
     * @return
     */
    int lockProductStock(@Param("productId") long productId, @Param("buyNum") int buyNum);

    /**
     * 解锁商品存储
     * @param productId
     * @param buyNum
     */
    void unlockProductStock(@Param("productId")Long productId, @Param("buyNum")Integer buyNum);
}
