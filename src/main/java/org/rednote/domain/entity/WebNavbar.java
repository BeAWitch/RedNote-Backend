package org.rednote.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 导航栏分类
 */
@Data
@TableName("web_navbar")
public class WebNavbar extends BaseEntity {

    /**
     * 标题
     */
    private String title;

    /**
     * 父级 ID
     */
    private Long pid;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 喜欢数量
     */
    private long likeCount;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 分类封面
     */
    private String normalCover;

    /**
     * 热门封面
     */
    private String hotCover;

    /**
     * 子导航栏
     */
    @TableField(exist = false)
    private List<WebNavbar> children = new ArrayList<WebNavbar>();
}
