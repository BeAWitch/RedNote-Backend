package org.rednote.search.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(name = "笔记搜索 DTO")
public class SearchNoteDTO implements Serializable {

    @Schema(description = "关键词")
    private String keyword;

    @Schema(description = "排序类型: 0-默认, 1-点赞排序, 2-时间排序")
    private Integer type;

    @Schema(description = "分类 ID")
    private Long cid;

    @Schema(description = "父分类 ID")
    private Long cpid;
}
