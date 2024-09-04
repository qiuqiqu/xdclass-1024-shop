package net.xdclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.enums.AddressStatusEnum;
import net.xdclass.interceptor.LoginInterceptor;
import net.xdclass.model.AddressDO;
import net.xdclass.mapper.AddressMapper;
import net.xdclass.model.LoginUser;
import net.xdclass.request.AddressAddReqeust;
import net.xdclass.service.AddressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.xdclass.vo.AddressVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 电商-公司收发货地址表 服务实现类
 * </p>

 * @since 2024-09-02
 */
@Service
@Slf4j
public class AddressServiceImpl extends ServiceImpl<AddressMapper, AddressDO> implements AddressService {
    @Autowired
    private AddressMapper addressMapper;


    @Override
    public AddressDO detail(Long id) {

        AddressDO addressDO = addressMapper.selectOne(new QueryWrapper<AddressDO>().eq("id",id));

        return addressDO;
    }

    /**
     * 新增收货地址
     * @param addressAddReqeust
     */
    @Override
    public void add(AddressAddReqeust addressAddReqeust) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        AddressDO addressDO = new AddressDO();
        addressDO.setCreateTime(new Date());
        addressDO.setUserId(loginUser.getId());

        BeanUtils.copyProperties(addressAddReqeust,addressDO);

        //是否有默认收货地址
        if(addressDO.getDefaultStatus() == AddressStatusEnum.DEFAULT_STATUS.getStatus()){
            //查找数据库是否有默认地址
            AddressDO defaultAddressDO = addressMapper.selectOne(new QueryWrapper<AddressDO>()
                    .eq("user_id",loginUser.getId())
                    .eq("default_status",AddressStatusEnum.DEFAULT_STATUS.getStatus()));

            if(defaultAddressDO != null){
                //修改为非默认收货地址
                defaultAddressDO.setDefaultStatus(AddressStatusEnum.COMMON_STATUS.getStatus());
                addressMapper.update(defaultAddressDO,new QueryWrapper<AddressDO>().eq("id",defaultAddressDO.getId()));
            }
        }

        int rows = addressMapper.insert(addressDO);

        log.info("新增收货地址:rows={},data={}",rows,addressDO);
    }

    /**
     * 查找用全部收货地址
     * @return
     */
    @Override
    public List<AddressVO> listUserAllAddress() {

        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        List<AddressDO> list = addressMapper.selectList(new QueryWrapper<AddressDO>().eq("user_id",loginUser.getId()));

        List<AddressVO> addressVOList =  list.stream().map(obj->{
            AddressVO addressVO = new AddressVO();
            BeanUtils.copyProperties(obj,addressVO);
            return addressVO;
        }).collect(Collectors.toList());

        return addressVOList;

    }

    /**
     * 根据id删除地址
     * @param addressId
     * @return
     */
    @Override
    public int del(int addressId) {
        int rows = addressMapper.delete(new QueryWrapper<AddressDO>().eq("id",addressId));
        return rows;
    }
}
