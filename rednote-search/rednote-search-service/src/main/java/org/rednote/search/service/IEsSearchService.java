package org.rednote.search.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.rednote.search.api.dto.SearchNoteDTO;
import org.rednote.search.api.entity.EsNote;

public interface IEsSearchService {

    /**
     * ES 搜索笔记
     *
     * @param currentPage   当前页
     * @param pageSize      每页大小
     * @param searchNoteDTO 搜索条件
     */
    Page<EsNote> searchNote(long currentPage, long pageSize, SearchNoteDTO searchNoteDTO);
}
