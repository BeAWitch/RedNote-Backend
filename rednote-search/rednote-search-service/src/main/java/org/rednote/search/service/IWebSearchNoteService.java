package org.rednote.search.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.rednote.note.api.entity.WebNavbar;
import org.rednote.search.api.dto.SearchNoteDTO;
import org.rednote.search.api.vo.NoteSearchVO;
import org.rednote.user.api.entity.WebUser;

import java.util.List;

/**
 * 笔记搜索
 */
public interface IWebSearchNoteService {

    /**
     * 搜索对应的笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param SearchNoteDTO   笔记
     */
    Page<NoteSearchVO> getNoteByDTO(long currentPage, long pageSize, SearchNoteDTO SearchNoteDTO);

    /**
     * 搜索对应的笔记
     *
     * @param SearchNoteDTO 笔记
     */
    List<WebNavbar> getCategoryAgg(SearchNoteDTO SearchNoteDTO);

    /**
     * 分页查询笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    Page<NoteSearchVO> getRecommendNote(long currentPage, long pageSize);

    /**
     * 获取推荐用户
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    Page<WebUser> getRecommendUser(long currentPage, long pageSize);

    /**
     * 获取热榜笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    Page<NoteSearchVO> getHotNote(long currentPage, long pageSize);
}
