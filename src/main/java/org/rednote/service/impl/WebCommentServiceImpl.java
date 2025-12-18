package org.rednote.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.rednote.domain.dto.CommentDTO;
import org.rednote.domain.dto.WSMessageDTO;
import org.rednote.domain.entity.WebComment;
import org.rednote.domain.entity.WebLikeOrFavorite;
import org.rednote.domain.entity.WebNote;
import org.rednote.domain.entity.WebUser;
import org.rednote.domain.entity.WebUserNoteRelation;
import org.rednote.domain.vo.CommentVO;
import org.rednote.enums.UncheckedMessageEnum;
import org.rednote.exception.RedNoteException;
import org.rednote.mapper.WebCommentMapper;
import org.rednote.mapper.WebLikeOrFavoriteMapper;
import org.rednote.mapper.WebNoteMapper;
import org.rednote.mapper.WebUserMapper;
import org.rednote.mapper.WebUserNoteRelationMapper;
import org.rednote.service.IWebChatService;
import org.rednote.service.IWebCommentService;
import org.rednote.utils.UserHolder;
import org.rednote.utils.WebSocketServer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评论
 */
@Service
@RequiredArgsConstructor
public class WebCommentServiceImpl extends ServiceImpl<WebCommentMapper, WebComment> implements IWebCommentService {
    
    private final WebNoteMapper noteMapper;
    private final WebUserMapper userMapper;
    private final WebLikeOrFavoriteMapper likeOrFavoriteMapper;
    private final WebUserNoteRelationMapper userNoteRelationMapper;
    private final IWebChatService chatService;
    private final WebSocketServer webSocketServer;

    /**
     * 保存评论
     *
     * @param commentDTO 评论
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public CommentVO saveCommentByDTO(CommentDTO commentDTO) {
        // 保存评论
        Long currentUid = UserHolder.getUserId();
        WebComment comment = BeanUtil.copyProperties(commentDTO, WebComment.class);
        comment.setUid(currentUid);
        this.save(comment);
        comment.setCreateTime(new Date());

        // 更新笔记评论数
        WebNote note = noteMapper.selectById(commentDTO.getNid());
        note.setCommentCount(note.getCommentCount() + 1);
        noteMapper.updateById(note);

        // 填充 VO
        CommentVO commentVo = BeanUtil.copyProperties(comment, CommentVO.class);
        WebUser user = userMapper.selectById(currentUid);
        commentVo.setUsername(user.getUsername())
                .setAvatar(user.getAvatar())
                .setTime(comment.getCreateTime().getTime());

        // 一级评论的二级评论数加 1
        if (ObjectUtil.isNotEmpty(commentDTO.getPid()) && commentDTO.getPid() != 0) {
            WebComment parentComment = this.getById(commentDTO.getPid());
            if (ObjectUtil.isNotEmpty(parentComment)) {
                parentComment.setLevelTwoCommentCount(parentComment.getLevelTwoCommentCount() + 1);
                this.updateById(parentComment);
            }
        }

        // 更新 Redis 和 Websocket 消息推送
        if (!commentDTO.getNoteUid().equals(currentUid)) {
            chatService.increaseUncheckedMessageCount(UncheckedMessageEnum.COMMENT, commentDTO.getNoteUid(), 1L);
            // Websocket 消息推送
            webSocketServer.sendMessage(new WSMessageDTO()
                    .setAcceptUid(commentDTO.getNoteUid())
                    .setType(UncheckedMessageEnum.COMMENT)
                    .setContent(1L)
            );
        }
        if (!commentDTO.getReplyUid().equals(commentDTO.getNoteUid()) && !commentDTO.getReplyUid().equals(currentUid)) {
            chatService.increaseUncheckedMessageCount(UncheckedMessageEnum.COMMENT, commentDTO.getReplyUid(), 1);
            // Websocket 消息推送
            webSocketServer.sendMessage(new WSMessageDTO()
                    .setAcceptUid(commentDTO.getReplyUid())
                    .setType(UncheckedMessageEnum.COMMENT)
                    .setContent(1L)
            );
        }

        return commentVo;
    }

    /**
     * 根据一级评论 ID 获取所有的二级评论
     *
     * @param currentPage  当前页
     * @param pageSize     分页数
     * @param levelOneCommentId 一级评论 ID
     */
    @Override
    public Page<CommentVO> getLevelTwoCommentByLevelOneCommentId(long currentPage, long pageSize, Long levelOneCommentId) {
        Page<CommentVO> result = new Page<>();
        Long currentUid = UserHolder.getUserId();
        // 获取二级评论
        Page<WebComment> levelTwoCommentPage = this.page(new Page<>((int) currentPage, (int) pageSize),
                new QueryWrapper<WebComment>()
                        .eq("pid", levelOneCommentId)
                        .orderByDesc("like_count")
                        .orderByDesc("create_time"));
        List<WebComment> levelTwoCommentList = levelTwoCommentPage.getRecords();
        long total = levelTwoCommentPage.getTotal();

        if (CollUtil.isNotEmpty(levelTwoCommentList)) {
            // 评论发布用户的信息
            Set<Long> uids = levelTwoCommentList.stream().map(WebComment::getUid).collect(Collectors.toSet());
            List<WebUser> users = userMapper.selectBatchIds(uids);
            Map<Long, WebUser> userMap = users.stream().collect(Collectors.toMap(WebUser::getId, user -> user));
            // 被评论用户的信息
            Set<Long> replyUids = levelTwoCommentList.stream().map(WebComment::getReplyUid).collect(Collectors.toSet());
            Map<Long, WebUser> replyUserMap = new HashMap<>(16);
            if (CollUtil.isNotEmpty(replyUids)) {
                List<WebUser> replyUsers = userMapper.selectBatchIds(replyUids);
                replyUserMap = replyUsers.stream().collect(Collectors.toMap(WebUser::getId, user -> user));
            }

            // 填充 VO
            List<CommentVO> commentVOS = new ArrayList<>();
            List<WebLikeOrFavorite> likeOrFavorites =
                    likeOrFavoriteMapper.selectList(new QueryWrapper<WebLikeOrFavorite>()
                            .eq("uid", currentUid)
                            .eq("type", 2));
            Set<Long> likeComments = likeOrFavorites.stream()
                    .map(WebLikeOrFavorite::getLikeOrFavoriteId)
                    .collect(Collectors.toSet());
            for (WebComment comment : levelTwoCommentList) {
                CommentVO commentVo = BeanUtil.copyProperties(comment, CommentVO.class);
                WebUser user = userMap.get(comment.getUid());
                commentVo.setUsername(user.getUsername())
                        .setAvatar(user.getAvatar())
                        .setTime(comment.getCreateTime().getTime())
                        .setIsLike(likeComments.contains(comment.getId()));
                WebUser replyUser = replyUserMap.get(comment.getReplyUid());
                if (replyUser != null) {
                    commentVo.setReplyUsername(replyUser.getUsername());
                }
                commentVOS.add(commentVo);
            }
            result.setRecords(commentVOS);
        }
        result.setTotal(total);
        return result;
    }

    /**
     * 获取当前用户收到的评论
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @Override
    public IPage<CommentVO> getCommentInfo(long currentPage, long pageSize) {
        Page<CommentVO> result = new Page<>();
        Long currentUid = UserHolder.getUserId();

        // 查询用户所有笔记的 ID
        List<Long> nidList = userNoteRelationMapper
                .selectList(new QueryWrapper<WebUserNoteRelation>().eq("uid", currentUid))
                .stream()
                .map(WebUserNoteRelation::getNid)
                .toList();
        // 查询所有与当前用户相关的评论
        Page<WebComment> commentPage = this.page(new Page<>((int) currentPage, (int) pageSize),
                new QueryWrapper<WebComment>().
                        or(e -> e.in("nid", nidList)
                                .or()
                                .eq("reply_uid", currentUid))
                        .ne("uid", currentUid).orderByDesc("create_time"));

        List<WebComment> commentList = commentPage.getRecords();
        long total = commentPage.getTotal();

        // 填充 VO
        List<CommentVO> commentVOList = new ArrayList<>();
        if (CollUtil.isNotEmpty(commentList)) {
            // 评论用户的信息
            Set<Long> uids = commentList.stream().map(WebComment::getUid).collect(Collectors.toSet());
            Map<Long, WebUser> userMap =
                    userMapper.selectBatchIds(uids).stream().collect(Collectors.toMap(WebUser::getId, user -> user));
            // 评论所在笔记的信息
            Set<Long> nids = commentList.stream().map(WebComment::getNid).collect(Collectors.toSet());
            Map<Long, WebNote> noteMap =
                    noteMapper.selectBatchIds(nids).stream().collect(Collectors.toMap(WebNote::getId, note -> note));
            // 被回复的评论的内容
            Set<Long> cids = commentList.stream()
                    .filter(item -> item.getPid() != 0)
                    .map(WebComment::getReplyId)
                    .collect(Collectors.toSet());
            Map<Long, WebComment> replyCommentMap = new HashMap<>(16);
            if (CollUtil.isNotEmpty(cids)) {
                replyCommentMap =
                        this.listByIds(cids).stream().collect(Collectors.toMap(WebComment::getId, comment -> comment));
            }

            for (WebComment comment : commentList) {
                CommentVO commentVo = BeanUtil.copyProperties(comment, CommentVO.class);
                WebUser user = userMap.get(comment.getUid());
                WebNote note = noteMap.get(comment.getNid());
                commentVo.setUsername(user.getUsername())
                        .setAvatar(user.getAvatar())
                        .setTime(comment.getCreateTime().getTime())
                        .setNoteCover(note.getNoteCover());

                if (comment.getPid() != 0) {
                    WebComment replyComment = replyCommentMap.get(comment.getReplyId());
                    commentVo.setReplyContent(replyComment.getContent());
                    if (!comment.getReplyUid().equals(currentUid)) {
                        WebUser replyUser = userMap.get(comment.getReplyUid());
                        commentVo.setReplyUsername(replyUser.getUsername());
                    }
                }
                commentVOList.add(commentVo);
            }
        }
        result.setRecords(commentVOList);
        result.setTotal(total);
        return result;
    }

    /**
     * 获取所有的一级评论并携带二级评论
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param noteId      笔记ID
     */
    @Override
    public Page<CommentVO> getCommentWithCommentByNoteId(long currentPage, long pageSize, Long noteId) {
        Page<CommentVO> result = new Page<>();
        // 先得到所有的一级评论
        Page<WebComment> levelOneCommentPage = this.page(new Page<>((int) currentPage, (int) pageSize),
                new QueryWrapper<WebComment>()
                        .eq("nid", noteId)
                        .eq("pid", "0")
                        .orderByDesc("like_count")
        );
        List<WebComment> levelOneCommentList = levelOneCommentPage.getRecords();
        if (CollUtil.isNotEmpty(levelOneCommentList)) {
            Set<Long> levelOneUids = levelOneCommentList.stream().map(WebComment::getUid).collect(Collectors.toSet());
            long levelOneTotal = levelOneCommentPage.getTotal();
            Long currentUid = UserHolder.getUserId();
            // 得到对应的二级评论
            List<Long> oneIds = levelOneCommentList.stream().map(WebComment::getId).collect(Collectors.toList());
            List<WebComment> levelTwoCommentList = this.list(new QueryWrapper<WebComment>()
                    .in("pid", oneIds)
                    .orderByDesc("like_count")
                    .orderByDesc("create_time")
            );
            Set<Long> levelTwoUids = levelTwoCommentList.stream().map(WebComment::getUid).collect(Collectors.toSet());
            levelOneUids.addAll(levelTwoUids);

            List<WebUser> users = userMapper.selectBatchIds(levelOneUids);
            Map<Long, WebUser> userMap = users.stream().collect(Collectors.toMap(WebUser::getId, user -> user));

            // 得到当前用户点赞的评论
            List<WebLikeOrFavorite> likeOrCollections = likeOrFavoriteMapper.selectList(
                    new QueryWrapper<WebLikeOrFavorite>()
                            .eq("uid", currentUid)
                            .eq("type", 2)
            );
            List<Long> likeComments = likeOrCollections.stream().map(WebLikeOrFavorite::getLikeOrFavoriteId).toList();

            Set<Long> replyUids = levelTwoCommentList.stream().map(WebComment::getReplyUid).collect(Collectors.toSet());
            Map<Long, WebUser> replyUserMap = new HashMap<>(16);
            if (!replyUids.isEmpty()) {
                List<WebUser> replyUsers = userMapper.selectBatchIds(replyUids);
                replyUserMap = replyUsers.stream().collect(Collectors.toMap(WebUser::getId, user -> user));
            }
            List<CommentVO> levelTwoCommentVOS = new ArrayList<>();
            for (WebComment levelTwoComment : levelTwoCommentList) {
                CommentVO commentVo = BeanUtil.copyProperties(levelTwoComment, CommentVO.class);
                WebUser user = userMap.get(levelTwoComment.getUid());
                commentVo.setUsername(user.getUsername())
                        .setAvatar(user.getAvatar())
                        .setTime(levelTwoComment.getCreateTime().getTime())
                        .setIsLike(likeComments.contains(levelTwoComment.getId()));
                WebUser replyUser = replyUserMap.get(levelTwoComment.getReplyUid());
                if (replyUser != null) {
                    commentVo.setReplyUsername(replyUser.getUsername());
                }
                levelTwoCommentVOS.add(commentVo);
            }

            Map<Long, List<CommentVO>> levelTwoCommentVoMap =
                    levelTwoCommentVOS.stream().collect(Collectors.groupingBy(CommentVO::getPid));
            List<CommentVO> commentVOList = new ArrayList<>();
            for (WebComment levelOneComment : levelOneCommentList) {
                CommentVO commentVo = BeanUtil.copyProperties(levelOneComment, CommentVO.class);
                WebUser user = userMap.get(levelOneComment.getUid());
                commentVo.setUsername(user.getUsername())
                        .setAvatar(user.getAvatar())
                        .setTime(levelOneComment.getCreateTime().getTime())
                        .setIsLike(likeComments.contains(levelOneComment.getId()));
                List<CommentVO> children = levelTwoCommentVoMap.get(levelOneComment.getId());

                if (children != null && children.size() > 3) {
                    children = children.subList(0, 3);
                }
                commentVo.setChildren(children);
                commentVOList.add(commentVo);
            }
            result.setRecords(commentVOList);
            result.setTotal(levelOneTotal);
        }
        return result;
    }

    /**
     * 删除评论
     *
     * @param commentId 评论 ID
     */
    @Override
    public void deleteCommentById(Long commentId) {
        if (ObjectUtil.isEmpty(commentId)) {
            throw new RedNoteException("评论删除失败，无效的评论 ID！");
        }
        removeById(commentId);
    }
}
