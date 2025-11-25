package org.rednote.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "搜索记录 VO")
public class RecordSearchVO implements Serializable {
    @Schema(description = "内容")
    private String content;
    @Schema(description = "搜索次数")
    private Long searchCount;
    @Schema(description = "时间")
    private Long time;
}
