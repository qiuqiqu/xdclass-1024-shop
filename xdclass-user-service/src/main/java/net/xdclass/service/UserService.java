package net.xdclass.service;

import net.xdclass.model.UserDO;
import com.baomidou.mybatisplus.extension.service.IService;
import net.xdclass.request.UserRegisterRequest;
import net.xdclass.util.JsonData;

/**
 * <p>
 *  服务类
 * </p>
 * @since 2024-09-02
 */
public interface UserService extends IService<UserDO> {

    /**
     * 用户注册
     * @param registerRequest
     * @return
     */
    JsonData register(UserRegisterRequest registerRequest);
}
