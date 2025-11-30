package org.rednote.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户
 */
@Data
@TableName("web_user")
public class WebUser extends BaseEntity {

    /**
     * 红书 ID
     */
    private Long hsId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * email
     */
    private String email;

    /**
     * 描述
     */
    private String description;

    /**
     * 用户状态，（0-正常，1-异常）
     */
    private String status;

    /**
     * 生日
     */
    private String birthday;

    /**
     * 地址
     */
    private String address;

    /**
     * 用户封面
     */
    private String userCover;

    /**
     * 用户标签，json 格式
     */
    private String tags;

    /**
     * 笔记数量
     */
    private Long noteCount;

    /**
     * 关注数量
     */
    private Long followCount;

    /**
     * 粉丝数量
     */
    private Long followerCount;

    /**
     * 最后登录 IP
     */
    private String loginIp;

    /**
     * 最后登录时间
     */
    private Date loginDate;

    /**
     * 备注
     */
    private String remark;
}
