package org.rednote.note.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.rednote.common.constant.RedisConstants;
import org.rednote.common.enums.ResultCodeEnum;
import org.rednote.common.exception.RedNoteException;
import org.rednote.common.utils.UserHolder;
import org.rednote.interaction.api.dto.WSMessageDTO;
import org.rednote.interaction.api.entity.WebComment;
import org.rednote.interaction.api.entity.WebFollow;
import org.rednote.interaction.api.entity.WebLikeOrFavorite;
import org.rednote.interaction.api.enums.UncheckedMessageEnum;
import org.rednote.interaction.api.util.WebSocketServer;
import org.rednote.note.api.dto.NoteDTO;
import org.rednote.note.api.entity.WebNote;
import org.rednote.note.api.entity.WebTag;
import org.rednote.note.api.entity.WebTagNoteRelation;
import org.rednote.note.api.entity.WebUserNoteRelation;
import org.rednote.note.api.vo.NoteVO;
import org.rednote.note.feign.InteractionServiceFeign;
import org.rednote.note.feign.OssServiceFeign;
import org.rednote.note.feign.UserServiceFeign;
import org.rednote.note.mapper.WebNoteMapper;
import org.rednote.note.mapper.WebTagMapper;
import org.rednote.note.mapper.WebTagNoteRelationMapper;
import org.rednote.note.mapper.WebUserNoteRelationMapper;
import org.rednote.note.service.IWebNoteService;
import org.rednote.search.api.dto.SearchNoteDTO;
import org.rednote.search.api.vo.NoteSearchVO;
import org.rednote.user.api.entity.WebUser;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

// TODO 消息队列
@Service
@RequiredArgsConstructor
public class WebNoteServiceImpl extends ServiceImpl<WebNoteMapper, WebNote> implements IWebNoteService {

    private final UserServiceFeign userServiceFeign;
    private final InteractionServiceFeign interactionServiceFeign;
    private final WebTagNoteRelationMapper tagNoteRelationMapper;
    private final WebUserNoteRelationMapper userNoteRelationMapper;
    private final WebTagMapper tagMapper;
    private final OssServiceFeign ossServiceFeign;
    private final StringRedisTemplate stringRedisTemplate;
    private final WebSocketServer webSocketServer;

    /**
     * 获取笔记
     *
     * @param noteId 笔记ID
     */
    @Override
    public NoteVO getNoteById(Long noteId) {
        WebNote note = this.getById(noteId);
        if (note == null) {
            throw new RedNoteException(ResultCodeEnum.FAIL);
        }

        // 更新浏览数
        note.setViewCount(note.getViewCount() + 1);
        saveOrUpdate(note);

        WebUser user = userServiceFeign.getUserById(note.getUid()).getData();
        NoteVO noteVo = BeanUtil.copyProperties(note, NoteVO.class);
        noteVo.setUsername(user.getUsername())
                .setAvatar(user.getAvatar())
                .setTime(note.getUpdateTime().getTime());

        boolean follow = interactionServiceFeign.isFollow(user.getId()).getData();
        noteVo.setIsFollow(follow);

        Long currentUid = UserHolder.getUserId();
        List<WebLikeOrFavorite> likeOrCollectionList =
                interactionServiceFeign.getLikeOrFavoriteByNidAndUid(noteId, currentUid);

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
        WebUser user = userServiceFeign.getUserById(currentUid).getData();
        user.setNoteCount(user.getNoteCount() + 1);
        userServiceFeign.updateUserById(user);

        // 保存笔记
        NoteDTO noteDTO = JSON.parseObject(noteData, NoteDTO.class);
        WebNote note = BeanUtil.copyProperties(noteDTO, WebNote.class);
        note.setUid(currentUid);
        note.setAuthor(user.getUsername());
        note.setAuditStatus(1); // TODO 审核初始值修改
        note.setNoteType(0);

        // 批量上传图片
        List<String> dataList = null;
        dataList = ossServiceFeign.uploadBatchFiles(files);
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
            ossServiceFeign.deleteBatchFiles(pathArr);
            // TODO 可以使用多线程优化，
            // 删除点赞图片，评论，标签关系，收藏关系
            interactionServiceFeign.deleteLikeOrFavoriteByObjId(noteId);
            List<WebComment> commentList = interactionServiceFeign.getCommentByNid(noteId);
            List<Long> cids = commentList.stream().map(WebComment::getId).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(cids)) {
                interactionServiceFeign.deleteLikeOrFavoriteByObjIds(cids);
                interactionServiceFeign.deleteCommentByIds(cids);
            }
            tagNoteRelationMapper.delete(new QueryWrapper<WebTagNoteRelation>().eq("nid", noteId));
            userNoteRelationMapper.delete(new QueryWrapper<WebUserNoteRelation>().eq("nid", noteId));
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
                newFileUrls = ossServiceFeign.uploadBatchFiles(files);
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
                ossServiceFeign.deleteBatchFiles(oldFileUrls);
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
    public boolean pinnedNote(Long noteId) {
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
     * 获取用户笔记关系
     *
     * @param userId 用户 ID
     */
    @Override
    public List<WebUserNoteRelation> getUserNoteRelationByUserIds(Long userId) {
        return userNoteRelationMapper.selectList(
                new LambdaQueryWrapper<>(WebUserNoteRelation.class).eq(WebUserNoteRelation::getUid, userId)
        );
    }

    /**
     * 获取笔记列表，按时间降序
     *
     * @param noteIds 笔记 ID 集合
     */
    @Override
    public List<WebNote> getByIdsOrderedByTime(List<Long> noteIds) {
        return lambdaQuery().in(WebNote::getId, noteIds).orderByDesc(WebNote::getCreateTime).list();
    }

    @Override
    public Page<WebNote> selectNotePage(Page<WebNote> page, SearchNoteDTO searchNoteDTO) {
        LambdaQueryWrapper<WebNote> queryWrapper = new LambdaQueryWrapper<>();

        // 关键词搜索（多字段模糊查询）
        if (StrUtil.isNotBlank(searchNoteDTO.getKeyword())) {
            String keyword = searchNoteDTO.getKeyword();
            queryWrapper.and(wrapper -> wrapper
                    .like(WebNote::getTitle, keyword)
                    .or().like(WebNote::getContent, keyword)
            );
        }

        // 分类条件查询
        if (ObjectUtil.isNotEmpty(searchNoteDTO.getCpid())) {
            queryWrapper.eq(WebNote::getCpid, searchNoteDTO.getCpid());
        }
        if (ObjectUtil.isNotEmpty(searchNoteDTO.getCid())) {
            queryWrapper.eq(WebNote::getCid, searchNoteDTO.getCid());
        }

        // 排序条件
        if (searchNoteDTO.getType() == 1) {
            queryWrapper.orderByDesc(WebNote::getLikeCount);
        } else if (searchNoteDTO.getType() == 2) {
            queryWrapper.orderByDesc(WebNote::getUpdateTime);
        }

        // 只查询审核通过的笔记
        queryWrapper.eq(WebNote::getAuditStatus, 1);

        // 查询
        return this.page(page, queryWrapper);
    }

    @Override
    public List<WebUserNoteRelation> getUserNoteRelationByUserId(Long userId) {
        return userNoteRelationMapper.selectList(
                new LambdaQueryWrapper<>(WebUserNoteRelation.class).eq(WebUserNoteRelation::getUid, userId)
        );
    }

    /**
     * 获取当前用户信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param userId      用户ID
     * @param type        类型（1：笔记，2：点赞，3：收藏）
     */
    @Override
    public Page<NoteSearchVO> getTrendByUser(long currentPage, long pageSize, Long userId, int type) {
        Page<NoteSearchVO> resultPage;
        if (type == 1) {
            resultPage = this.getNoteByUser(currentPage, pageSize, userId);
        } else if (type == 2) {
            resultPage = this.getLikeOrFavoriteNoteByUser(currentPage, pageSize, userId, 1);
        } else {
            resultPage = this.getLikeOrFavoriteNoteByUser(currentPage, pageSize, userId, 3);
        }
        return resultPage;
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
        List<Long> followerIdList = interactionServiceFeign.getFollowByFid(uid).stream()
                .map(WebFollow::getUid)
                .toList();
        for (Long followerId : followerIdList) {
            String key = RedisConstants.TREND_KEY + followerId;
            stringRedisTemplate.opsForZSet().add(key, nid.toString(), System.currentTimeMillis());

            // 更新未查看动态数量
            interactionServiceFeign.increaseUncheckedMessageCount(UncheckedMessageEnum.TREND, followerId, 1L);
            // Websocket 消息推送
            webSocketServer.sendMessage(new WSMessageDTO()
                    .setAcceptUid(followerId)
                    .setType(UncheckedMessageEnum.TREND)
                    .setContent(1L)
            );
        }
    }

    private Page<NoteSearchVO> getLikeOrFavoriteNoteByUser(long currentPage, long pageSize, Long userId, int type) {
        Page<NoteSearchVO> noteSearchVoPage = new Page<>();
        Page<WebLikeOrFavorite> likeOrFavoritePage;

        // 所有点赞或收藏过的笔记
        likeOrFavoritePage =
                interactionServiceFeign.getLikeOrFavoriteByUidAndTypeOrderByTime(currentPage, pageSize, userId, type);
        List<Long> likeOrFavoriteIdList =
                likeOrFavoritePage.getRecords().stream().map(WebLikeOrFavorite::getLikeOrFavoriteId).toList();
        long total = likeOrFavoritePage.getTotal();
        if (CollUtil.isEmpty(likeOrFavoriteIdList)) {
            return null;
        }
        List<WebNote> likeOrFavoriteNoteList = this.listByIds(likeOrFavoriteIdList);

        Long currentUserId = UserHolder.getUserId();
        // 是否点赞
        List<Long> likedNoteIds = interactionServiceFeign.getLikeOrFavoriteByUidAndType(currentUserId, 1)
                .stream()
                .map(WebLikeOrFavorite::getLikeOrFavoriteId)
                .toList();

        // 笔记对应的用户
        Map<Long, WebUser> userMap = likedNoteIds.stream()
                .map(likedNoteId -> {
                    Long uid = this.getById(likedNoteId).getUid();
                    return userServiceFeign.getUserById(uid).getData();
                })
                .collect(Collectors.toMap(WebUser::getId, user -> user));

        // 填充 VO
        List<NoteSearchVO> noteSearchVOList = likeOrFavoriteNoteList.stream().map(note -> {
            NoteSearchVO noteSearchVO = BeanUtil.copyProperties(note, NoteSearchVO.class);
            noteSearchVO.setIsLike(likedNoteIds.contains(note.getId()));
            noteSearchVO.setAvatar(userMap.get(note.getUid()).getAvatar());
            noteSearchVO.setUsername(userMap.get(note.getUid()).getUsername());
            return noteSearchVO;
        }).toList();

        noteSearchVoPage.setRecords(noteSearchVOList);
        noteSearchVoPage.setTotal(total);
        return noteSearchVoPage;
    }

    private Page<NoteSearchVO> getNoteByUser(long currentPage, long pageSize, Long userId) {
        Page<NoteSearchVO> noteSearchVoPage = new Page<>();
        // 获取用户发布的笔记
        Page<WebNote> notePage;
        notePage = this.page(new Page<>(currentPage, pageSize),
                new QueryWrapper<WebNote>()
                        .eq("uid", userId)
                        .orderByDesc("pinned", "update_time"));
        List<WebNote> noteList = notePage.getRecords();
        long total = notePage.getTotal();
        if (CollUtil.isEmpty(noteList)) {
            return null;
        }

        // 得到用户的信息
        WebUser user = userServiceFeign.getUserById(userId).getData();

        // 当前用户是否点赞
        Long currentUserId = UserHolder.getUserId();
        Set<Long> likeOrFavoriteIds = interactionServiceFeign.getLikeOrFavoriteByUidAndType(currentUserId, 1)
                .stream()
                .map(WebLikeOrFavorite::getLikeOrFavoriteId)
                .collect(Collectors.toSet());

        // 填充 VO
        List<NoteSearchVO> noteSearchVOList = noteList.stream()
                .map(note -> {
                    NoteSearchVO noteSearchVo = BeanUtil.copyProperties(note, NoteSearchVO.class);
                    noteSearchVo.setUsername(user.getUsername())
                            .setAvatar(user.getAvatar())
                            .setIsLike(likeOrFavoriteIds.contains(note.getId()))
                            .setTime(note.getUpdateTime().getTime());
                    return noteSearchVo;
                }).toList();

        noteSearchVoPage.setRecords(noteSearchVOList);
        noteSearchVoPage.setTotal(total);
        return noteSearchVoPage;
    }
}
