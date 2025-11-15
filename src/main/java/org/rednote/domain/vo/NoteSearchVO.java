package org.rednote.domain.vo;

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
    private String id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "笔记封面")
    private String noteCover;

    @Schema(description = "笔记类型")
    private String noteType;

    @Schema(description = "笔记封面高度")
    private Integer noteCoverHeight;

    @Schema(description = "分类 ID")
    private String cid;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "父分类 ID")
    private String cpid;

    @Schema(description = "父分类名称")
    private String categoryParentName;

    @Schema(description = "用户ID")
    private String uid;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "图片地址")
    private String urls;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "是否置顶")
    private String pinned;

    @Schema(description = "审核状态")
    private String auditStatus;

    @Schema(description = "状态")
    private String status;

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
