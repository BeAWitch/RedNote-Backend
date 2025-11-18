package org.rednote.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "基础 VO")
public class BaseVO<T> extends PageInfo<T> {

    @Schema(description = "唯一 UID")
    private String uid;

    @Schema(description = "状态")
    private Integer status;
}
