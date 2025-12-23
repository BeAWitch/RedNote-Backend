package org.rednote.interaction.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(name = "滚动页面返回结果")
@Data
public class ScrollResult<T> {
    @Schema(description = "返回的实体列表")
    private List<T> list;
    @Schema(description = "查询结果的最小时间戳，前端页面的下一次请求需要携带这个参数")
    private Long minTime;
    @Schema(description = "最小时间戳的实体的个数，前端页面的下一次请求需要携带这个参数")
    private Integer offset;
}
