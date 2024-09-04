package net.xdclass.mapper;

import net.xdclass.model.AddressDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 电商-公司收发货地址表 Mapper 接口
 * </p>
 * @since 2024-09-02
 */
@Mapper
public interface AddressMapper extends BaseMapper<AddressDO> {

}
