package net.xdclass.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>

 * @since 2024-09-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user")
public class UserDO implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 昵称
     */
    private String name;

    /**
     * 密	码
     */
    private String pwd;

    /**
     * 头像
     */
    private String headImg;

    /**
     * ⽤户签名
     */
    private String slogan;

    /**
     * 0表示	⼥，1表示男
     */
    private Integer sex;

    /**
     * 积	分
     */
    private Integer points;

    private Date createTime;

    /**
     * 邮	箱
     */
    private String mail;

    /**
     * 盐，⽤于个⼈敏感信息处理
     */
    private String secret;


}
