package org.rednote.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.rednote.domain.dto.CommentDTO;
import org.rednote.domain.entity.WebComment;
import org.rednote.domain.vo.CommentVO;

/**
 * 评论
 */
public interface IWebCommentService extends IService<WebComment> {

    /**
     * 保存评论
     *
     * @param commentDTO 评论
     */
    CommentVO saveCommentByDTO(CommentDTO commentDTO);

    /**
     * 根据一级评论 ID 获取所有的二级评论
     *
     * @param currentPage  当前页
     * @param pageSize     分页数
     * @param levelOneCommentId 一级评论 ID
     */
    Page<CommentVO> getLevelTwoCommentByLevelOneCommentId(long currentPage, long pageSize, Long levelOneCommentId);

    /**
     * 获取当前用户的评论集
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    IPage<CommentVO> getCommentInfo(long currentPage, long pageSize);

    /**
     * 获取所有的一级评论并携带二级评论
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param noteId      笔记ID
     */
    Page<CommentVO> getCommentWithCommentByNoteId(long currentPage, long pageSize, Long noteId);

    /**
     * 删除评论
     *
     * @param commentId 评论 ID
     */
    void deleteCommentById(Long commentId);
}
