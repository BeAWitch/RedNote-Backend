package org.rednote.search.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rednote.common.utils.UserHolder;
import org.rednote.interaction.api.dto.LikeOrFavoriteDTO;
import org.rednote.note.api.entity.WebNavbar;
import org.rednote.note.api.entity.WebNote;
import org.rednote.note.api.entity.WebTag;
import org.rednote.note.api.entity.WebTagNoteRelation;
import org.rednote.search.api.dto.SearchNoteDTO;
import org.rednote.search.api.vo.NoteSearchVO;
import org.rednote.search.feign.InteractionServiceFeign;
import org.rednote.search.feign.NoteServiceFeign;
import org.rednote.search.feign.UserServiceFeign;
import org.rednote.search.service.IWebSearchNoteService;
import org.rednote.user.api.entity.WebUser;
import org.springframework.stereotype.Service;

import java.util.List;

// TODO
//  Redis 缓存
//  智能推荐
//  es

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSearchNoteServiceImpl implements IWebSearchNoteService {

    private final UserServiceFeign userServiceFeign;
    private final NoteServiceFeign noteServiceFeign;
    private final InteractionServiceFeign interactionServiceFeign;

    /**
     * 搜索对应的笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param searchNoteDTO   笔记查询条件
     */
    @Override
    public Page<NoteSearchVO> getNoteByDTO(long currentPage, long pageSize, SearchNoteDTO searchNoteDTO) {
        Page<WebNote> notePage = new Page<>(currentPage, pageSize);
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

        // 执行查询
        Page<WebNote> resultPage = noteServiceFeign.selectNotePage(notePage, queryWrapper);

        // 转换为 VO 并填充额外信息
        IPage<NoteSearchVO> noteSearchVOIPage = resultPage.convert(this::convertToNoteSearchVO);

        return new Page<NoteSearchVO>()
                .setRecords(noteSearchVOIPage.getRecords())
                .setTotal(noteSearchVOIPage.getTotal());
    }

    /**
     * 搜索对应的分类
     *
     * @param SearchNoteDTO 笔记
     * @return
     */
    @Override
    public List<WebNavbar> getCategoryAgg(SearchNoteDTO SearchNoteDTO) {
        return noteServiceFeign.selectCategoryList(
                new LambdaQueryWrapper<>(WebNavbar.class).like(WebNavbar::getTitle, SearchNoteDTO.getKeyword())
        );
    }

    /**
     * 分页查询笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @Override
    public Page<NoteSearchVO> getRecommendNote(long currentPage, long pageSize) {
        // 构建查询条件
        Page<WebNote> notePage = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<WebNote> queryWrapper = new LambdaQueryWrapper<>();

        // 排序条件
        queryWrapper.orderByDesc(WebNote::getLikeCount);

        // 只查询审核通过的笔记
        queryWrapper.eq(WebNote::getAuditStatus, 1);

        // 执行查询
        Page<WebNote> resultPage = noteServiceFeign.selectNotePage(notePage, queryWrapper);

        // 转换为 VO 并填充额外信息
        IPage<NoteSearchVO> noteSearchVOIPage = resultPage.convert(this::convertToNoteSearchVO);
        return new Page<NoteSearchVO>()
                .setRecords(noteSearchVOIPage.getRecords())
                .setTotal(noteSearchVOIPage.getTotal());
    }

    /**
     * 获取推荐用户
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @Override
    public Page<WebUser> getRecommendUser(long currentPage, long pageSize) {
        return userServiceFeign.selectUserPage(new Page<>(currentPage, pageSize), new LambdaQueryWrapper<>());
    }

    /**
     * 获取热榜笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    @Override
    public Page<NoteSearchVO> getHotNote(long currentPage, long pageSize) {
        // 构建查询条件
        Page<WebNote> notePage = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<WebNote> queryWrapper = new LambdaQueryWrapper<>();

        // 排序条件
        queryWrapper.orderByDesc(WebNote::getLikeCount);

        // 只查询审核通过的笔记
        queryWrapper.eq(WebNote::getAuditStatus, 1);

        // 执行查询
        Page<WebNote> resultPage = noteServiceFeign.selectNotePage(notePage, queryWrapper);

        // 转换为 VO 并填充额外信息
        IPage<NoteSearchVO> noteSearchVOIPage = resultPage.convert(this::convertToNoteSearchVO);
        return new Page<NoteSearchVO>()
                .setRecords(noteSearchVOIPage.getRecords())
                .setTotal(noteSearchVOIPage.getTotal());
    }

    /**
     * 将 WebNote 转换为 NoteSearchVO 并填充额外信息
     */
    private NoteSearchVO convertToNoteSearchVO(WebNote note) {
        NoteSearchVO vo = new NoteSearchVO();

        // 基本字段映射
        vo.setId(note.getId());
        vo.setTitle(note.getTitle());
        vo.setContent(note.getContent());
        vo.setNoteCover(note.getNoteCover());
        vo.setNoteType(note.getNoteType());
        vo.setNoteCoverHeight(note.getNoteCoverHeight());
        vo.setCid(note.getCid());
        vo.setCpid(note.getCpid());
        vo.setUid(note.getUid());
        vo.setUrls(note.getUrls());
        vo.setPinned(note.getPinned());
        vo.setAuditStatus(note.getAuditStatus());
        vo.setLikeCount(note.getLikeCount());
        vo.setViewCount(note.getViewCount());
        vo.setIsLoading(false);

        // 填充用户信息
        fillUserInfo(vo, note.getUid());

        // 填充分类信息
        fillCategoryInfo(vo, note.getCid(), note.getCpid());

        // 填充标签信息
        fillTagsInfo(vo, note.getId());

        // 填充点赞状态（需要当前登录用户ID）
        fillLikeStatus(vo, note.getId());

        return vo;
    }

    /**
     * 填充用户信息
     */
    private void fillUserInfo(NoteSearchVO vo, Long uid) {
        if (ObjectUtil.isEmpty(uid)) return;

        WebUser user = userServiceFeign.getUserById(uid).getData();
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setAvatar(user.getAvatar());
        }
    }

    /**
     * 填充分类信息
     */
    private void fillCategoryInfo(NoteSearchVO vo, Long cid, Long cpid) {
        // 填充二级分类名称
        if (ObjectUtil.isNotEmpty(cid)) {
            WebNavbar category = noteServiceFeign.getCategoryById(cid);
            if (category != null) {
                vo.setCategoryName(category.getTitle());
            }
        }

        // 填充一级分类名称
        if (ObjectUtil.isNotEmpty(cpid)) {
            WebNavbar parentCategory = noteServiceFeign.getCategoryById(cpid);
            if (parentCategory != null) {
                vo.setCategoryParentName(parentCategory.getTitle());
            }
        }
    }

    /**
     * 填充标签信息
     */
    private void fillTagsInfo(NoteSearchVO vo, Long noteId) {
        if (ObjectUtil.isEmpty(noteId)) return;

        // 查询笔记关联的标签 ID 列表
        List<WebTagNoteRelation> relations = noteServiceFeign.getTagNoteRelationByNid(noteId);

        if (relations.isEmpty()) {
            vo.setTags("[]"); // 空数组 JSON
            return;
        }

        // 提取标签 ID 列表
        List<Long> tagIds = relations.stream()
                .map(WebTagNoteRelation::getTid)
                .toList();

        // 批量查询标签信息
        List<WebTag> tags = noteServiceFeign.getTagByIds(tagIds);

        // 提取标签标题并转换为 JSON 数组字符串
        List<String> tagTitles = tags.stream()
                .map(WebTag::getTitle)
                .filter(StrUtil::isNotBlank)
                .toList();

        // 5. 转换为 JSON 数组字符串
        String tagsJson = JSON.toJSONString(tagTitles);
        vo.setTags(tagsJson);
    }

    /**
     * 填充点赞状态
     */
    private void fillLikeStatus(NoteSearchVO vo, Long noteId) {
        // 获取当前登录用户 ID
        Long currentUserId = UserHolder.getUserId();

        if (ObjectUtil.isNotEmpty(currentUserId) && ObjectUtil.isNotEmpty(noteId)) {
            LikeOrFavoriteDTO likeOrFavoriteDTO = new LikeOrFavoriteDTO();
            likeOrFavoriteDTO.setLikeOrFavoriteId(noteId);
            likeOrFavoriteDTO.setType(1);
            vo.setIsLike(interactionServiceFeign.isLikeOrFavorite(likeOrFavoriteDTO).getData());
        } else {
            vo.setIsLike(false);
        }
    }
}
