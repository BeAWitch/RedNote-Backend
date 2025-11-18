package org.rednote.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Web 访问记录
 */
@Data
@TableName("web_visit")
public class WebVisit extends BaseEntity {

    /**
     * 用户 uid
     */
    private String userUid;

    /**
     * 用户 ip
     */
    private String ip;

    /**
     * ip 来源
     */
    private String ipSource;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 用户访问行为（点击了文章，点击了标签，点击了分类，进行了搜索）
     */
    private String behavior;

    /**
     * 文章 uid，标签 uid，分类 uid
     */
    private String moduleUid;

    /**
     * 附加数据（比如搜索内容）
     */
    private String otherData;

    /**
     * 内容(点击的博客名，点击的标签名，搜索的内容，点击的作者)
     */
    @TableField(exist = false)
    private String content;

    /**
     * 行为名称
     */
    @TableField(exist = false)
    private String behaviorContent;
}
