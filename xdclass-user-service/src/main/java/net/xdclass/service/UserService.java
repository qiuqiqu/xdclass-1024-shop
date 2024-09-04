package net.xdclass.service;

import net.xdclass.model.UserDO;
import com.baomidou.mybatisplus.extension.service.IService;
import net.xdclass.request.UserLoginRequest;
import net.xdclass.request.UserRegisterRequest;
import net.xdclass.util.JsonData;
import net.xdclass.vo.UserVO;

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

    /**
     * 用户登录
     * @param loginRequest
     * @return
     */
    JsonData login(UserLoginRequest loginRequest);

    /**
     * 查询个人信息详情
     * @return
     */
    UserVO findUserDetail();
}
