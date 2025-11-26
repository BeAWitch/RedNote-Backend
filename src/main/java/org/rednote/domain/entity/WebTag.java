package org.rednote.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 标签
 */
@Data
@TableName("web_tag")
public class WebTag extends BaseEntity {

    /**
     * 使用次数
     */
    private Long likeCount;

    /**
     * 标题
     */
    private String title;
}
