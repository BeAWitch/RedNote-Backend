package org.rednote.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.rednote.domain.dto.SearchNoteDTO;
import org.rednote.domain.entity.WebNavbar;
import org.rednote.domain.entity.WebNote;
import org.rednote.domain.entity.WebUser;
import org.rednote.domain.vo.NoteSearchVO;

import java.util.List;

/**
 * 笔记
 */
public interface IWebSearchNoteService extends IService<WebNote> {

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

    /**
     * 增加笔记
     *
     * @param noteSearchVo 笔记
     */
    void addNote(NoteSearchVO noteSearchVo);

    /**
     * 修改笔记
     *
     * @param noteSearchVo 笔记
     */
    void updateNote(NoteSearchVO noteSearchVo);

    /**
     * 批量增加笔记
     */
    void addNoteBulkData();

    /**
     * 删除es中的笔记
     *
     * @param noteId 笔记 ID
     */
    void deleteNote(String noteId);

    /**
     * 清空笔记
     */
    void delNoteBulkData();

    /**
     * 重置
     */
    void refreshNoteData();
}
