package net.xdclass.service;

import net.xdclass.model.AddressDO;
import com.baomidou.mybatisplus.extension.service.IService;
import net.xdclass.request.AddressAddReqeust;
import net.xdclass.vo.AddressVO;

import java.util.List;

/**
 * <p>
 * 电商-公司收发货地址表 服务类
 * </p>
 * @since 2024-09-02
 */
public interface AddressService extends IService<AddressDO> {
    /**
     * 根据id查找地址详情
     * @param id
     * @return
     */
    AddressDO detail(Long id);

    /**
     * 新增收货地址
     * @param addressAddReqeust
     */
    void add(AddressAddReqeust addressAddReqeust);

    /**
     * 查找用户全部收货地址
     * @return
     */
    List<AddressVO> listUserAllAddress();

    /**
     * 根据id删除地址
     * @param addressId
     * @return
     */
    int del(int addressId);

}
