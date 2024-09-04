package net.xdclass.service;

import net.xdclass.model.AddressDO;
import com.baomidou.mybatisplus.extension.service.IService;
import net.xdclass.request.AddressAddReqeust;

/**
 * <p>
 * 电商-公司收发货地址表 服务类
 * </p>
 * @since 2024-09-02
 */
public interface AddressService extends IService<AddressDO> {
    AddressDO detail(Long id);

    /**
     * 新增收货地址
     * @param addressAddReqeust
     */
    void add(AddressAddReqeust addressAddReqeust);
}
