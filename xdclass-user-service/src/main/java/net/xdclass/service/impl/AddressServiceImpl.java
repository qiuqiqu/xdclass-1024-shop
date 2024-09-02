package net.xdclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import net.xdclass.model.AddressDO;
import net.xdclass.mapper.AddressMapper;
import net.xdclass.service.AddressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 电商-公司收发货地址表 服务实现类
 * </p>

 * @since 2024-09-02
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, AddressDO> implements AddressService {
    @Autowired
    private AddressMapper addressMapper;


    @Override
    public AddressDO detail(Long id) {

        AddressDO addressDO = addressMapper.selectOne(new QueryWrapper<AddressDO>().eq("id",id));


        return addressDO;
    }
}
