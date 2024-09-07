package net.xdclass.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 新用户优惠券请求
 * @Version 1.0
 **/
@ApiModel
@Data
public class NewUserCouponRequest {


    @ApiModelProperty(value = "用户Id",example = "19")
    @JsonProperty("user_id")
    private long userId;


    @ApiModelProperty(value = "名称",example = "二当家小D")
    @JsonProperty("name")
    private String name;



}