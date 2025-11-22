package org.rednote.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "分页参数")
public class PageInfo {

    @Schema(description = "关键字")
    private String keyword;

    @Schema(description = "当前页")
    private Long currentPage;

    @Schema(description = "页大小")
    private Long pageSize;
}
