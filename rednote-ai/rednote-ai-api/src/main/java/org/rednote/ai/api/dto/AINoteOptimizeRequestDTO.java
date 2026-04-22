package org.rednote.ai.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(name = "AI 笔记优化请求参数 DTO")
@Data
public class AINoteOptimizeRequestDTO {

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "图片 URL 列表")
    private List<String> imageUrls;

    @Schema(description = "原标题")
    private String title;

    @Schema(description = "原文案")
    private String content;

    @Schema(description = "原标签")
    private List<String> tags;
}
