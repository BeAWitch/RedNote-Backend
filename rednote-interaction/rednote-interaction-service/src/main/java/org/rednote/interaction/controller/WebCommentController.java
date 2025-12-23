package org.rednote.interaction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.rednote.common.domain.dto.Result;
import org.rednote.interaction.api.dto.CommentDTO;
import org.rednote.interaction.api.vo.CommentVO;
import org.rednote.interaction.service.IWebCommentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论
 */
@Tag(name = "评论管理", description = "用户评论相关接口")
@RequestMapping("/web/comment")
@RestController
@RequiredArgsConstructor
public class WebCommentController {

    private final IWebCommentService commentService;

    @Operation(summary = "保存评论", description = "保存评论")
    @PostMapping("saveCommentByDTO")
    public Result<?> saveCommentByDTO(@Parameter(description = "评论") @RequestBody CommentDTO commentDTO) {
        CommentVO commentVo = commentService.saveCommentByDTO(commentDTO);
        return Result.ok(commentVo);
    }

    @Operation(summary = "根据一级评论 ID 获取所有二级评论", description = "根据一级评论 ID 获取所有二级评论")
    @GetMapping("getLevelTwoCommentByLevelOneCommentId/{currentPage}/{pageSize}")
    public Result<?> getLevelTwoCommentByLevelOneCommentId(
            @Parameter(description = "当前页") @PathVariable long currentPage,
            @Parameter(description = "分页数") @PathVariable long pageSize,
            @Parameter(description = "一级评论 ID") Long levelOneCommentId) {
        IPage<CommentVO> pageInfo =
                commentService.getLevelTwoCommentByLevelOneCommentId(currentPage, pageSize, levelOneCommentId);
        return Result.ok(pageInfo);
    }

    @Operation(summary = "获取当前用户收到的评论", description = "获取当前用户收到的评论")
    @GetMapping("getCommentInfo/{currentPage}/{pageSize}")
    public Result<?> getCommentInfo(@Parameter(description = "当前页") @PathVariable long currentPage,
                                      @Parameter(description = "分页数") @PathVariable long pageSize) {
        IPage<CommentVO> pageInfo = commentService.getCommentInfo(currentPage, pageSize);
        return Result.ok(pageInfo);
    }

    @Operation(summary = "获取所有的一级评论并携带二级评论", description = "获取所有的一级评论并携带二级评论")
    @GetMapping("getCommentWithCommentByNoteId/{currentPage}/{pageSize}")
    public Result<?> getCommentWithCommentByNoteId(@Parameter(description = "当前页") @PathVariable long currentPage,
                                                   @Parameter(description = "分页数") @PathVariable long pageSize,
                                                   @Parameter(description = "笔记 ID") Long noteId) {
        Page<CommentVO> pageInfo = commentService.getCommentWithCommentByNoteId(currentPage, pageSize, noteId);
        return Result.ok(pageInfo);
    }

    @Operation(summary = "删除评论", description = "删除评论")
    @GetMapping("deleteCommentById")
    public Result<?> deleteCommentById(@Parameter(description = "评论 ID") Long commentId) {
        commentService.deleteCommentById(commentId);
        return Result.ok();
    }
}
