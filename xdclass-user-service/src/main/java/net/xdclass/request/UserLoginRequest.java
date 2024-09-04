package net.xdclass.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户登录请求
 * @Version 1.0
 **/

@Data
@ApiModel(value = "登录对象",description = "用户登录请求对象")
public class UserLoginRequest {

    @ApiModelProperty(value = "邮箱", example = "1961237983@qq.com")
    private String mail;

    @ApiModelProperty(value = "密码", example = "123456")
    private String pwd;


}
