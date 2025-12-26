package org.rednote.interaction.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.rednote.common.constant.RedisConstants;
import org.rednote.common.utils.UserHolder;
import org.rednote.interaction.api.dto.LikeOrFavoriteDTO;
import org.rednote.interaction.api.dto.WSMessageDTO;
import org.rednote.interaction.api.entity.WebComment;
import org.rednote.interaction.api.entity.WebLikeOrFavorite;
import org.rednote.interaction.api.enums.UncheckedMessageEnum;
import org.rednote.interaction.api.util.WebSocketServer;
import org.rednote.interaction.api.vo.CommentVO;
import org.rednote.interaction.api.vo.LikeOrFavoriteVO;
import org.rednote.interaction.feign.NoteServiceFeign;
import org.rednote.interaction.feign.UserServiceFeign;
import org.rednote.interaction.mapper.WebCommentMapper;
import org.rednote.interaction.mapper.WebLikeOrFavoriteMapper;
import org.rednote.interaction.service.IWebChatService;
import org.rednote.interaction.service.IWebLikeOrFavoriteService;
import org.rednote.note.api.entity.WebAlbum;
import org.rednote.note.api.entity.WebAlbumNoteRelation;
import org.rednote.note.api.entity.WebNote;
import org.rednote.user.api.entity.WebUser;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 点赞/收藏
 */
@Service
@RequiredArgsConstructor
public class WebLikeOrFavoriteServiceImpl extends ServiceImpl<WebLikeOrFavoriteMapper, WebLikeOrFavorite> implements IWebLikeOrFavoriteService {

    private final UserServiceFeign userServiceFeign;
    private final NoteServiceFeign noteServiceFeign;
    private final WebCommentMapper commentMapper;
    private final IWebChatService chatService;
    private final StringRedisTemplate stringRedisTemplate;
    private final WebSocketServer webSocketServer;

    /**
     * 点赞或收藏
     *
     * @param likeOrFavoriteDTO 点赞收藏实体
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void likeOrFavoriteByDTO(LikeOrFavoriteDTO likeOrFavoriteDTO) {
        Long currentUid = UserHolder.getUserId();
        if (ObjectUtil.isEmpty(currentUid)) {
            // 未登录
            return;
        }

        if (isLikeOrFavorite(likeOrFavoriteDTO)) {
            // 取消
            // 更新数据库
            this.remove(new QueryWrapper<WebLikeOrFavorite>()
                    .eq("uid", currentUid)
                    .eq("like_or_favorite_id", likeOrFavoriteDTO.getLikeOrFavoriteId())
                    .eq("type", likeOrFavoriteDTO.getType()));
            this.updateLikeFavoriteCount(likeOrFavoriteDTO, -1);
            // 更新 Redis
            String key = getRedisPrefix(likeOrFavoriteDTO) + likeOrFavoriteDTO.getLikeOrFavoriteId();
            stringRedisTemplate.opsForSet().remove(key, currentUid.toString());
        } else {
            // 点赞或收藏
            // 更新数据库
            WebLikeOrFavorite likeOrFavorite = BeanUtil.copyProperties(likeOrFavoriteDTO, WebLikeOrFavorite.class);
            likeOrFavorite.setUid(currentUid);
            this.save(likeOrFavorite);
            this.updateLikeFavoriteCount(likeOrFavoriteDTO, 1);
            // 更新 Redis
            String key = getRedisPrefix(likeOrFavoriteDTO) + likeOrFavoriteDTO.getLikeOrFavoriteId();
            stringRedisTemplate.opsForSet().add(key, currentUid.toString());

            // 消息通知
            // 不是当前用户才进行通知
            if (!likeOrFavoriteDTO.getNotifyUid().equals(currentUid)) {
                Long publishUid = likeOrFavoriteDTO.getNotifyUid();
                // 更新 Redis
                chatService.increaseUncheckedMessageCount(
                        UncheckedMessageEnum.LIKE_OR_FAVORITE,
                        publishUid,
                        1L
                );
                // Websocket 消息推送
                webSocketServer.sendMessage(new WSMessageDTO()
                        .setAcceptUid(publishUid)
                        .setType(UncheckedMessageEnum.LIKE_OR_FAVORITE)
                        .setContent(1L)
                );
            }
        }
    }

    /**
     * 是否点赞或收藏
     *
     * @param likeOrFavoriteDTO 点赞收藏实体
     */
    @Override
    public boolean isLikeOrFavorite(LikeOrFavoriteDTO likeOrFavoriteDTO) {
        Long currentUid = UserHolder.getUserId();
        if (ObjectUtil.isEmpty(currentUid)) {
            // 未登录
            return false;
        }
        String key = getRedisPrefix(likeOrFavoriteDTO) + likeOrFavoriteDTO.getLikeOrFavoriteId();
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, currentUid.toString());
        return BooleanUtil.isTrue(isMember);
    }

    /**
     * 获取当前用户最新的点赞和收藏信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @Override
    public Page<LikeOrFavoriteVO> getLikeAndFavoriteInfo(long currentPage, long pageSize) {
        Page<LikeOrFavoriteVO> result = new Page<>();
        Long currentUid = UserHolder.getUserId();

        Page<WebLikeOrFavorite> likeOrFavoritePage = this.page(new Page<>((int) currentPage, (int) pageSize),
                new QueryWrapper<WebLikeOrFavorite>()
                        .eq("notify_uid", currentUid)
                        .orderByDesc("create_time"));
        List<WebLikeOrFavorite> likeOrFavoriteList = likeOrFavoritePage.getRecords();
        long total = likeOrFavoritePage.getTotal();

        // TODO 可以使用多线程优化
        // 获取详细数据
        // 得到所有用户
        List<Long> uids = likeOrFavoriteList.stream().map(WebLikeOrFavorite::getUid).toList();
        Map<Long, WebUser> userMap = new HashMap<>(16);
        if (CollUtil.isNotEmpty(uids)) {
            userMap = userServiceFeign.getUserByIds(uids)
                    .stream().collect(Collectors.toMap(WebUser::getId, user -> user));
        }
        // 笔记
        List<Long> nids = likeOrFavoriteList.stream()
                .filter(e -> e.getType() == 1 || e.getType() == 3)
                .map(WebLikeOrFavorite::getLikeOrFavoriteId)
                .toList();
        Map<Long, WebNote> noteMap = new HashMap<>(16);
        if (CollUtil.isNotEmpty(nids)) {
            noteMap = noteServiceFeign.getNoteByIds(nids)
                    .stream().collect(Collectors.toMap(WebNote::getId, note -> note));
        }
        // 评论
        Set<Long> cids = likeOrFavoriteList.stream().filter(e -> e.getType() == 2).map(WebLikeOrFavorite::getLikeOrFavoriteId).collect(Collectors.toSet());
        Map<Long, CommentVO> commentVoMap = new HashMap<>(16);
        if (!cids.isEmpty()) {
            List<WebComment> commentList = commentMapper.selectBatchIds(cids);
            List<Long> noteIds = commentList.stream().map(WebComment::getNid).toList();
            Map<Long, WebNote> noteMap1 = noteServiceFeign.getNoteByIds(noteIds).stream().collect(Collectors.toMap(WebNote::getId, note -> note));

            commentList.forEach((item -> {
                CommentVO commentVo = BeanUtil.copyProperties(item, CommentVO.class);
                WebNote note = noteMap1.get(item.getNid());
                commentVo.setNoteCover(note.getNoteCover());
                commentVoMap.put(item.getId(), commentVo);
            }));
        }
        // 专辑
        List<Long> aids = likeOrFavoriteList.stream()
                .filter(e -> e.getType() == 4)
                .map(WebLikeOrFavorite::getLikeOrFavoriteId).toList();
        Map<Long, WebAlbum> albumMap = new HashMap<>(16);
        if (!aids.isEmpty()) {
            albumMap = noteServiceFeign.getAlbumByIds(aids).stream().collect(Collectors.toMap(WebAlbum::getId, album -> album));
        }

        // 填充 VO
        List<LikeOrFavoriteVO> likeOrFavoriteVOList = new ArrayList<>();
        for (WebLikeOrFavorite model : likeOrFavoriteList) {
            LikeOrFavoriteVO likeOrFavoriteVo = new LikeOrFavoriteVO();
            WebUser user = userMap.get(model.getUid());
            likeOrFavoriteVo.setUid(user.getId())
                    .setUsername(user.getUsername())
                    .setAvatar(user.getAvatar())
                    .setType(model.getType());
            switch (model.getType()) {
                case 2:
                    CommentVO commentVo = commentVoMap.get(model.getLikeOrFavoriteId());
                    likeOrFavoriteVo.setItemId(commentVo.getId())
                            .setItemCover(commentVo.getNoteCover())
                            .setContent(commentVo.getContent());
                    break;
                case 4:
                    WebAlbum album = albumMap.get(model.getLikeOrFavoriteId());
                    likeOrFavoriteVo.setItemId(album.getId())
                            .setItemCover(album.getAlbumCover())
                            .setContent(album.getTitle());
                    break;
                default:
                    WebNote note = noteMap.get(model.getLikeOrFavoriteId());
                    likeOrFavoriteVo.setItemId(note.getId())
                            .setItemCover(note.getNoteCover());
                    break;
            }
            likeOrFavoriteVOList.add(likeOrFavoriteVo);
        }
        result.setRecords(likeOrFavoriteVOList);
        result.setTotal(total);
        return result;
    }

    /**
     * 点赞
     */
    private void updateLikeFavoriteCount(LikeOrFavoriteDTO likeOrFavoriteDTO, int val) {
        switch (likeOrFavoriteDTO.getType()) {
            case 1:
                WebNote likeNote = noteServiceFeign.getNoteById(likeOrFavoriteDTO.getLikeOrFavoriteId());
                likeNote.setLikeCount(likeNote.getLikeCount() + val);
                noteServiceFeign.updateNote(likeNote);
                break;
            case 2:
                WebComment comment = commentMapper.selectById(likeOrFavoriteDTO.getLikeOrFavoriteId());
                if (ObjectUtil.isNotNull(comment)) {
                    comment.setLikeCount(comment.getLikeCount() + val);
                    commentMapper.updateById(comment);
                }
                break;
            case 3:
                Long currentUid = UserHolder.getUserId();
                WebNote favoriteNote = noteServiceFeign.getNoteById(likeOrFavoriteDTO.getLikeOrFavoriteId());
                favoriteNote.setFavoriteCount(favoriteNote.getFavoriteCount() + val);
                noteServiceFeign.updateNote(favoriteNote);

                WebAlbumNoteRelation albumNoteRelation = new WebAlbumNoteRelation();
                albumNoteRelation.setNid(favoriteNote.getId());
                if (val == 1) {
                    WebAlbum album = noteServiceFeign.getAlbumByIdAndType(currentUid, 0);
                    Integer imgCount = favoriteNote.getCount();
                    if (ObjectUtil.isEmpty(album)) {
                        album = new WebAlbum();
                        album.setTitle("默认专辑");
                        album.setUid(currentUid);
                        album.setAlbumCover(favoriteNote.getNoteCover());
                        album.setNoteCount(Long.valueOf(imgCount));
                        noteServiceFeign.addAlbum(album);
                    } else {
                        album.setNoteCount(album.getNoteCount() + imgCount);
                        if (StrUtil.isBlank(album.getAlbumCover())) {
                            album.setAlbumCover(favoriteNote.getNoteCover());
                        }
                        noteServiceFeign.updateAlbum(album);
                    }
                    albumNoteRelation.setAid(album.getId());
                    noteServiceFeign.addAlbumNoteRelation(albumNoteRelation);
                } else {
                    List<WebAlbumNoteRelation> albumNoteRelationList = noteServiceFeign.getAlbumNoteRelationByNid(favoriteNote.getId());
                    List<Long> aids = albumNoteRelationList.stream()
                            .map(WebAlbumNoteRelation::getAid)
                            .toList();
                    List<WebAlbum> albumList = noteServiceFeign.getAlbumByIds(aids);
                    WebAlbum album = albumList.stream()
                            .filter(item -> item.getUid().equals(currentUid))
                            .findFirst()
                            .orElse(null);
                    Integer imgCount = favoriteNote.getCount();
                    long nums = 0;
                    if (album != null) {
                        nums = album.getNoteCount() - imgCount;
                        if (nums <= 0) {
                            album.setAlbumCover(null);
                        }
                        album.setNoteCount(nums);
                        noteServiceFeign.updateAlbum(album);
                        noteServiceFeign.deleteAlbumNoteRelationByAidAndNid(album.getId(), favoriteNote.getId());
                    }
                }
                break;
            default:
                // 收藏专辑
                WebAlbum favoriteAlbum = noteServiceFeign.getAlbumById(likeOrFavoriteDTO.getLikeOrFavoriteId());
                favoriteAlbum.setFavoriteCount(favoriteAlbum.getFavoriteCount() + val);
                noteServiceFeign.updateAlbum(favoriteAlbum);
                break;
        }
    }

    private String getRedisPrefix(LikeOrFavoriteDTO likeOrFavoriteDTO) {
        return switch (likeOrFavoriteDTO.getType()) {
            case 1 -> RedisConstants.NOTE_LIKE_KEY;
            case 2 -> RedisConstants.COMMENT_LIKE_KEY;
            case 3 -> RedisConstants.NOTE_FAVORITE_KEY;
            case 4 -> RedisConstants.ALBUM_FAVORITE_KEY;
            default -> "";
        };
    }
}
