package org.rednote.note.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.rednote.common.constant.RedisConstants;
import org.rednote.common.enums.ResultCodeEnum;
import org.rednote.common.utils.UserHolder;
import org.rednote.note.domain.dto.NoteDTO;
import org.rednote.note.domain.entity.WebNote;
import org.rednote.note.domain.entity.WebTag;
import org.rednote.note.domain.entity.WebTagNoteRelation;
import org.rednote.note.domain.entity.WebUserNoteRelation;
import org.rednote.note.domain.vo.NoteVO;
import org.rednote.note.mapper.WebNoteMapper;
import org.rednote.note.mapper.WebTagMapper;
import org.rednote.note.mapper.WebTagNoteRelationMapper;
import org.rednote.note.mapper.WebUserNoteRelationMapper;
import org.rednote.note.service.IWebNoteService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

// TODO 消息队列
@Service
@RequiredArgsConstructor
public class WebNoteServiceImpl extends ServiceImpl<WebNoteMapper, WebNote> implements IWebNoteService {

    private final WebUserMapper userMapper;
    private final WebTagNoteRelationMapper tagNoteRelationMapper;
    private final WebUserNoteRelationMapper userNoteRelationMapper;
    private final WebTagMapper tagMapper;
    private final IWebFollowService followService;
    private final WebLikeOrFavoriteMapper likeOrCollectMapper;
    private final WebCommentMapper commentMapper;
    private final WebAlbumNoteRelationMapper albumNoteRelationMapper;
    private final IWebOssService ossService;
    private final WebFollowMapper followMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final IWebChatService chatService;
    private final WebSocketServer webSocketServer;

    /**
     * 获取笔记
     *
     * @param noteId 笔记ID
     */
    @Override
    public NoteVO getNoteById(String noteId) {
        WebNote note = this.getById(noteId);
        if (note == null) {
            throw new RedNoteException(ResultCodeEnum.FAIL);
        }

        // 更新浏览数
        note.setViewCount(note.getViewCount() + 1);
        saveOrUpdate(note);

        WebUser user = userMapper.selectById(note.getUid());
        NoteVO noteVo = BeanUtil.copyProperties(note, NoteVO.class);
        noteVo.setUsername(user.getUsername())
                .setAvatar(user.getAvatar())
                .setTime(note.getUpdateTime().getTime());

        boolean follow = followService.isFollow(user.getId());
        noteVo.setIsFollow(follow);

        Long currentUid = UserHolder.getUserId();
        List<WebLikeOrFavorite> likeOrCollectionList =
                likeOrCollectMapper.selectList(new QueryWrapper<WebLikeOrFavorite>()
                        .eq("like_or_favorite_id", noteId)
                        .eq("uid", currentUid));

        Set<Integer> types = likeOrCollectionList.stream().map(WebLikeOrFavorite::getType).collect(Collectors.toSet());
        noteVo.setIsLike(types.contains(1));
        noteVo.setIsFavorite(types.contains(3));

        //得到标签
        List<WebTagNoteRelation> tagNoteRelationList =
                tagNoteRelationMapper.selectList(new QueryWrapper<WebTagNoteRelation>().eq("nid", noteId));
        List<Long> tids = tagNoteRelationList.stream().map(WebTagNoteRelation::getTid).collect(Collectors.toList());

        if (!tids.isEmpty()) {
            List<WebTag> tagList = tagMapper.selectBatchIds(tids);
            noteVo.setTagList(tagList);
        }

        return noteVo;
    }

    /**
     * 新增笔记
     *
     * @param noteData 笔记对象
     * @param files    文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveNoteByDTO(String noteData, MultipartFile[] files) {
        Long currentUid = UserHolder.getUserId();
        // 更新用户笔记数量
        WebUser user = userMapper.selectById(currentUid);
        user.setNoteCount(user.getNoteCount() + 1);
        userMapper.updateById(user);

        // 保存笔记
        NoteDTO noteDTO = JSON.parseObject(noteData, NoteDTO.class);
        WebNote note = BeanUtil.copyProperties(noteDTO, WebNote.class);
        note.setUid(currentUid);
        note.setAuthor(user.getUsername());
        note.setAuditStatus(1); // TODO 审核初始值修改
        note.setNoteType(0);

        // 批量上传图片
        List<String> dataList = null;
        try {
            dataList = ossService.saveBatch(files);
        } catch (Exception e) {
            throw new RedNoteException("图片上传失败");
        }
        String[] urlArr = Objects.requireNonNull(dataList).toArray(new String[dataList.size()]);
        String urls = JSONUtil.toJsonStr(urlArr);
        note.setUrls(urls);
        note.setNoteCover(urlArr[0]);
        this.save(note);

        // 绑定标签关系
        bindTagsToNote(note, noteDTO);
        // 绑定用户与笔记关系
        bindUserToNote(note);

        // 消息推送
        pushToFollowers(currentUid, note.getId());

        return note.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNoteByIds(List<String> noteIds) {
        List<WebNote> noteList = this.listByIds(noteIds);
        // TODO 这里需要优化，数据一致性问题
        noteList.forEach(item -> {
            Long noteId = item.getId();
            removeById(noteId);

            String urls = item.getUrls();
            JSONArray objects = JSONUtil.parseArray(urls);
            Object[] array = objects.toArray();
            List<String> pathArr = new ArrayList<>();
            for (Object o : array) {
                pathArr.add((String) o);
            }
            ossService.batchDelete(pathArr);
            // TODO 可以使用多线程优化，
            // 删除点赞图片，评论，标签关系，收藏关系
            likeOrCollectMapper.delete(new QueryWrapper<WebLikeOrFavorite>()
                    .eq("like_or_favorite_id", noteId));
            List<WebComment> commentList =
                    commentMapper.selectList(new QueryWrapper<WebComment>().eq("nid", noteId));
            List<Long> cids = commentList.stream().map(WebComment::getId).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(cids)) {
                likeOrCollectMapper.delete(new QueryWrapper<WebLikeOrFavorite>()
                        .in("like_or_favorite_id", cids));
                commentMapper.deleteByIds(cids);
            }
            tagNoteRelationMapper.delete(new QueryWrapper<WebTagNoteRelation>().eq("nid", noteId));
            userNoteRelationMapper.delete(new QueryWrapper<WebUserNoteRelation>().eq("nid", noteId));
            albumNoteRelationMapper.delete(new QueryWrapper<WebAlbumNoteRelation>().eq("nid", noteId));
        });
        this.removeBatchByIds(noteIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNoteByDTO(String noteData, MultipartFile[] files) {
        Long currentUid = UserHolder.getUserId();
        NoteDTO noteDTO = JSON.parseObject(noteData, NoteDTO.class);

        // 查询原笔记信息
        WebNote originalNote = this.getById(noteDTO.getId());
        if (originalNote == null) {
            throw new RuntimeException("笔记不存在");
        }
        if (!Objects.equals(currentUid, originalNote.getUid())) {
            throw new RuntimeException("非作者修改笔记");
        }

        // 上传文件
        List<String> newFileUrls = null;
        if (files != null && files.length > 0) {
            try {
                newFileUrls = ossService.saveBatch(files);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 设置新文件 URL
        WebNote note = BeanUtil.copyProperties(noteDTO, WebNote.class);
        if (CollUtil.isNotEmpty(newFileUrls)) {
            String newUrls = JSONUtil.toJsonStr(newFileUrls);
            note.setUrls(newUrls);
            note.setNoteCover(newFileUrls.get(0));
        }

        // 更新信息
        note.setAuditStatus(1); // TODO 审核初始值修改
        note.setNoteType(noteDTO.getType());
        note.setUpdateTime(new Date());

        // 更新笔记
        boolean updateSuccess = this.updateById(note);
        if (!updateSuccess) {
            throw new RuntimeException("更新笔记失败");
        }

        // 删除旧图
        if (StrUtil.isNotBlank(originalNote.getUrls())) {
            JSONArray objects = JSONUtil.parseArray(originalNote.getUrls());
            List<String> oldFileUrls = objects.toList(String.class);
            if (!oldFileUrls.isEmpty()) {
                ossService.batchDelete(oldFileUrls);
            }
        }

        // 删除原来的标签绑定关系
        tagNoteRelationMapper.delete(new QueryWrapper<WebTagNoteRelation>().eq("nid", note.getId()));
        // 重新绑定标签关系
        bindTagsToNote(note, noteDTO);

        // 推送至粉丝收件箱
        // pushToFollowers(currentUid, note.getId()); // 暂存，更新时先不推送
    }

    @Override
    public boolean pinnedNote(String noteId) {
        Long currentUid = UserHolder.getUserId();
        WebNote note = this.getById(noteId);
        if (note.getPinned() == 1) {
            note.setPinned(0);
        } else {
            List<WebNote> noteList = this.list(new QueryWrapper<WebNote>().eq("uid", currentUid));
            long count = noteList.stream().filter(item -> item.getPinned() == 1).count();
            if (count >= 3) {
                throw new RedNoteException("最多只能置顶3个笔记");
            }
            note.setPinned(1);
        }
        return this.updateById(note);
    }


    /**
     * 绑定标签于笔记，并创建不存在的标签
     *
     * @param note
     * @param noteDTO
     */
    private void bindTagsToNote(WebNote note, NoteDTO noteDTO) {
        List<String> tagList = noteDTO.getTagList();
        List<WebTagNoteRelation> tagNoteRelationList = new ArrayList<>();
        Map<String, WebTag> allTags = tagMapper.selectList(new QueryWrapper<>())
                .stream().collect(Collectors.toMap(WebTag::getTitle, tag -> tag));

        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                WebTagNoteRelation tagNoteRelation = new WebTagNoteRelation();
                if (allTags.containsKey(tag)) {
                    WebTag tagModel = allTags.get(tag);
                    tagNoteRelation.setTid(tagModel.getId());
                } else {
                    WebTag model = new WebTag();
                    model.setTitle(tag);
                    model.setLikeCount(1L);
                    tagMapper.insertOrUpdate(model);
                    tagNoteRelation.setTid(model.getId());
                }
                tagNoteRelation.setNid(note.getId());
                tagNoteRelationList.add(tagNoteRelation);
            }
            tagNoteRelationMapper.insertOrUpdate(tagNoteRelationList);
        }
    }

    /**
     * 绑定用户于笔记
     *
     * @param note
     */
    private void bindUserToNote(WebNote note) {
        Long userID = note.getUid();
        Long noteID = note.getId();
        WebUserNoteRelation webUserNoteRelation = new WebUserNoteRelation();
        webUserNoteRelation.setNid(noteID);
        webUserNoteRelation.setUid(userID);
        userNoteRelationMapper.insert(webUserNoteRelation);
    }

    /**
     * 将笔记推送至用户的收件箱
     */
    private void pushToFollowers(Long uid, Long nid) {
        List<Long> followerIdList =
                followMapper.selectList(new QueryWrapper<WebFollow>().eq("fid", uid)).stream()
                        .map(WebFollow::getUid)
                        .toList();
        for (Long followerId : followerIdList) {
            String key = RedisConstants.TREND_KEY + followerId;
            stringRedisTemplate.opsForZSet().add(key, nid.toString(), System.currentTimeMillis());

            // 更新未查看动态数量
            chatService.increaseUncheckedMessageCount(UncheckedMessageEnum.TREND, followerId, 1L);
            // Websocket 消息推送
            webSocketServer.sendMessage(new WSMessageDTO()
                    .setAcceptUid(followerId)
                    .setType(UncheckedMessageEnum.TREND)
                    .setContent(1L)
            );
        }
    }
}
