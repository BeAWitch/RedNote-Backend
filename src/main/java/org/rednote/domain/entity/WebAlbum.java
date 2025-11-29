package org.rednote.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 专辑
 */
@Data
@TableName("web_album")
public class WebAlbum extends BaseEntity {

    /**
     * 标题
     */
    private String title;

    /**
     * 用户 ID
     */
    private Long uid;

    /**
     * 专辑封面图
     */
    private String albumCover;

    /**
     * 专辑类型（0：默认）
     */
    private Integer type;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 专辑中笔记数量
     */
    private Long noteCount;

    /**
     * 收藏数量
     */
    private Long favoriteCount;

}
