package org.rednote.interaction.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.rednote.common.domain.entity.BaseEntity;

/**
 * 关注
 */
@Data
@TableName("web_follow")
public class WebFollow extends BaseEntity {

    /**
     * 用户 ID
     */
    private Long uid;

    /**
     * 关注用户 ID
     */
    private Long fid;

}
