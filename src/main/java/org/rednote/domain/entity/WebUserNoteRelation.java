package org.rednote.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户-笔记
 */
@Data
@TableName("web_user_note_relation")
public class WebUserNoteRelation extends BaseEntity {

    /**
     * 笔记ID
     */
    private String nid;

    /**
     * 用户ID
     */
    private String uid;
}
