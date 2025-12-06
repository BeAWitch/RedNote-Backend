package org.rednote.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.rednote.domain.entity.WebLikeOrFavorite;
import org.rednote.domain.entity.WebNote;
import org.rednote.domain.entity.WebUser;
import org.rednote.domain.vo.NoteSearchVO;
import org.rednote.exception.RedNoteException;
import org.rednote.mapper.WebLikeOrFavoriteMapper;
import org.rednote.mapper.WebNoteMapper;
import org.rednote.mapper.WebUserMapper;
import org.rednote.service.IWebOssService;
import org.rednote.service.IWebUserService;
import org.rednote.utils.UserHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户
 */
@Service
@RequiredArgsConstructor
public class WebUserServiceImpl extends ServiceImpl<WebUserMapper, WebUser> implements IWebUserService {

    private final WebUserMapper userMapper;
    private final WebNoteMapper noteMapper;
    private final WebLikeOrFavoriteMapper likeOrFavoriteMapper;
    private final IWebOssService ossService;

    /**
     * 获取当前用户信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param userId      用户ID
     * @param type        类型（1：笔记，2：点赞，3：收藏）
     */
    @Override
    public Page<NoteSearchVO> getTrendByUser(long currentPage, long pageSize, String userId, int type) {
        Page<NoteSearchVO> resultPage;
        if (type == 1) {
            resultPage = this.getNoteByUser(currentPage, pageSize, userId);
        } else if (type == 2){
            resultPage = this.getLikeOrFavoriteNoteByUser(currentPage, pageSize, userId, 1);
        } else {
            resultPage = this.getLikeOrFavoriteNoteByUser(currentPage, pageSize, userId, 3);
        }
        return resultPage;
    }

    /**
     * 更新用户信息
     *
     * @param userData 用户数据 JSON 字符串
     * @param avatar   上传的头像
     */
    @Override
    public WebUser updateUser(String userData, MultipartFile avatar) {
        WebUser user = JSON.parseObject(userData, WebUser.class);

        WebUser webUser = userMapper.selectById(user.getId());
        if (ObjectUtil.isEmpty(webUser)) {
            throw new RedNoteException("用户信息更新失败，该用户不存在！");
        }

        webUser.setUsername(user.getUsername());
        webUser.setDescription(user.getDescription());
        webUser.setTags(user.getTags());

        // 上传头像
        String avatarUrl = ossService.save(avatar);
        if (StrUtil.isEmpty(avatarUrl)) {
            throw new RedNoteException("头像上传失败！");
        }
        webUser.setAvatar(avatarUrl);

        userMapper.updateById(webUser);

        return webUser;
    }

    /**
     * 查找用户信息
     *
     * @param keyword 关键词
     */
    @Override
    public Page<WebUser> getUserByKeyword(long currentPage, long pageSize, String keyword) {
        Page<WebUser> resultPage;
        resultPage = userMapper.selectPage(new Page<>((int) currentPage, (int) pageSize),
                new QueryWrapper<WebUser>().like("username", keyword));
        return resultPage;
    }

    private Page<NoteSearchVO> getLikeOrFavoriteNoteByUser(long currentPage, long pageSize, String userId, int type) {
        Page<NoteSearchVO> noteSearchVoPage = new Page<>();
        Page<WebLikeOrFavorite> likeOrFavoritePage;

        // 所有点赞或收藏过的笔记
        likeOrFavoritePage = likeOrFavoriteMapper.selectPage(new Page<>(currentPage, pageSize),
                new QueryWrapper<WebLikeOrFavorite>()
                        .eq("uid", userId)
                        .eq("type", type)
                        .orderByDesc("create_time"));
        List<Long> likeOrFavoriteIdList =
                likeOrFavoritePage.getRecords().stream().map(WebLikeOrFavorite::getLikeOrFavoriteId).toList();
        long total = likeOrFavoritePage.getTotal();
        if (CollUtil.isEmpty(likeOrFavoriteIdList)) {
            return null;
        }
        List<WebNote> likeOrFavoriteNoteList = noteMapper.selectBatchIds(likeOrFavoriteIdList);

        Long currentUserId = UserHolder.getUserId();
        // 是否点赞
        List<Long> likedNoteIds =
                likeOrFavoriteMapper.selectList(new QueryWrapper<WebLikeOrFavorite>()
                        .eq("uid", currentUserId)
                        .eq("type", 1))
                        .stream()
                        .map(WebLikeOrFavorite::getLikeOrFavoriteId)
                        .toList();

        // 笔记对应的用户
        Map<Long, WebUser> userMap = likedNoteIds.stream()
                .map(likedNoteId -> userMapper.selectById(noteMapper.selectById(likedNoteId).getUid()))
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

    private Page<NoteSearchVO> getNoteByUser(long currentPage, long pageSize, String userId) {
        Page<NoteSearchVO> noteSearchVoPage = new Page<>();
        // 获取用户发布的笔记
        Page<WebNote> notePage;
        notePage = noteMapper.selectPage(new Page<>(currentPage, pageSize),
                new QueryWrapper<WebNote>()
                        .eq("uid", userId)
                        .orderByDesc("pinned", "update_time"));
        List<WebNote> noteList = notePage.getRecords();
        long total = notePage.getTotal();
        if (CollUtil.isEmpty(noteList)) {
            return null;
        }

        // 得到用户的信息
        WebUser user = userMapper.selectById(userId);

        // 当前用户是否点赞
        Long currentUserId = UserHolder.getUserId();
        Set<Long> likeOrFavoriteIds = likeOrFavoriteMapper.selectList(new QueryWrapper<WebLikeOrFavorite>()
                        .eq("uid", currentUserId)
                        .eq("type", 1))
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
