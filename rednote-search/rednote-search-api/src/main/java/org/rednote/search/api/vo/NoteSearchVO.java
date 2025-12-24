package org.rednote.search.api.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "笔记搜索 VO")
public class NoteSearchVO implements Serializable {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "笔记封面")
    private String noteCover;

    @Schema(description = "笔记类型")
    private Integer noteType;

    @Schema(description = "笔记封面高度")
    private Integer noteCoverHeight;

    @Schema(description = "分类 ID")
    private Long cid;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "父分类 ID")
    private Long cpid;

    @Schema(description = "父分类名称")
    private String categoryParentName;

    @Schema(description = "用户 ID")
    private Long uid;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "图片地址")
    private String urls;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "是否置顶")
    private Integer pinned;

    @Schema(description = "审核状态")
    private Integer auditStatus;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "是否点赞")
    private Boolean isLike;

    @Schema(description = "点赞数")
    private Long likeCount;

    @Schema(description = "浏览数")
    private Long viewCount;

    @Schema(description = "时间")
    private Long time;

    @Schema(description = "是否加载中")
    private Boolean isLoading;
}
