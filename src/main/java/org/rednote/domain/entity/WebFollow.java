package org.rednote.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 关注
 */
@Data
@TableName("web_follow")
public class WebFollow extends BaseEntity {

    /**
     * 用户ID
     */
    private String uid;

    /**
     * 关注用户ID
     */
    private String fid;

}
