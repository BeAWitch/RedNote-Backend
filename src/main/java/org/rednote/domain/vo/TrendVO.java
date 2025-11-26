package org.rednote.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name = "动态 VO")
public class TrendVO implements Serializable {

    @Schema(description = "笔记 ID")
    private Long nid;

    @Schema(description = "用户 ID")
    private Long uid;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "时间")
    private Long time;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "图片地址列表")
    private List<String> imgUrls;

    @Schema(description = "浏览数")
    private Long viewCount;

    @Schema(description = "点赞数")
    private Long likeCount;

    @Schema(description = "评论数")
    private Long commentCount;

    @Schema(description = "是否点赞")
    private Boolean isLike;

    @Schema(description = "是否加载中")
    private Boolean isLoading;
}
