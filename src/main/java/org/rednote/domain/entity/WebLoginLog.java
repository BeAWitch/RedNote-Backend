
package org.rednote.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 访问记录表
 */
@TableName("web_login_log")
@Data
public class WebLoginLog extends BaseEntity{

    /**
     * 用户 ID
     */
    private Long uid;

    /**
     * 登录状态（0-成功，1-失败）
     */
    private Integer status;

    /**
     * 登录 IP 地址
     */
    private String ipaddr;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 提示消息
     */
    private String msg;
}
