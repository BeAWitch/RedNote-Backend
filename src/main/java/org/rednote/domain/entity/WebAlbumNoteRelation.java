package org.rednote.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 专辑-笔记
 */
@Data
@TableName("web_album_note_relation")
public class WebAlbumNoteRelation extends BaseEntity {

    /**
     * 专辑 ID
     */
    private Long aid;

    /**
     * 笔记 ID
     */
    private Long nid;

}
