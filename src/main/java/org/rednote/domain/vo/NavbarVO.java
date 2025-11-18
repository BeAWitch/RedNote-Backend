package org.rednote.domain.vo;

import cn.hutool.core.lang.tree.TreeNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(name = "导航栏 VO")
public class NavbarVO extends TreeNode<NavbarVO> implements Serializable {

    @Schema(description = "标题")
    private String title;

    @Schema(description = "点赞数")
    private long likeCount;
}
