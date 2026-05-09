package org.rednote.ai.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(name = "生成大纲结果")
public class CreativeOutlineVO {

    @Schema(description = "大纲 ID")
    private String outlineId;

    @Schema(description = "大纲内容")
    private Outline outline;

    @Schema(description = "引用来源（最多 8 个）")
    private List<CreativeSourceVO> sources;

    @Data
    @Schema(name = "大纲")
    public static class Outline {

        @Schema(description = "标题候选")
        private List<String> titleCandidates;

        @Schema(description = "章节")
        private List<Section> sections;

        @Schema(description = "标签候选")
        private List<String> tagCandidates;
    }

    @Data
    @Schema(name = "章节")
    public static class Section {
        @Schema(description = "章节标题")
        private String title;

        @Schema(description = "要点")
        private List<Point> points;
    }

    @Data
    @Schema(name = "要点")
    public static class Point {
        @Schema(description = "要点文本")
        private String text;

        @Schema(description = "引用来源 ID 列表（S1..S8）")
        private List<String> citations;
    }
}
