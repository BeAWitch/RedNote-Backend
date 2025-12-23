package org.rednote.note.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.rednote.common.domain.entity.BaseEntity;

/**
 * 标签-笔记
 */
@Data
@TableName("web_tag_note_relation")
public class WebTagNoteRelation extends BaseEntity {

    /**
     * 笔记 ID
     */
    private Long nid;

    /**
     * 标签ID
     */
    private Long tid;
}
