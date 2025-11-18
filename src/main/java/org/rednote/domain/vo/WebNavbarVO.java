package org.rednote.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(name = "网页导航栏 VO")
public class WebNavbarVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "父级 ID")
    private Long pid;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "分类描述")
    private String description;

    @Schema(description = "喜欢数量")
    private long likeCount;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "分类封面")
    private String normalCover;

    @Schema(description = "热门封面")
    private String hotCover;

    @Schema(description = "子导航栏")
    private List<WebNavbarVO> children = new ArrayList<WebNavbarVO>();
}
