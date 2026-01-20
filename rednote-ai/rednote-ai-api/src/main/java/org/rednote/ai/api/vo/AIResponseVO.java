package org.rednote.ai.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(name = "AI 响应数据 VO")
@Data
public class AIResponseVO {

    @Schema(description = "AI 返回的消息")
    private String message;

    @Schema(description = "响应是否成功")
    private Boolean success;

    @Schema(description = "错误信息")
    private String error;

    @Schema(description = "错误码")
    private String errorCode;

    @Schema(description = "响应时间")
    private Long responseTime;
}
