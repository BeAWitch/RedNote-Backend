package org.rednote.search.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(name = "笔记搜索记录")
public class SearchRecordDTO implements Serializable {

    @Schema(description = "关键词")
    private String keyword;

    @Schema(description = "笔记 ID")
    private Long uid;
}
