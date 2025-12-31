package org.rednote.search.service;

import java.util.List;

public interface IEsSyncService {

    /**
     * 全量同步笔记到 ES
     */
    void fullSyncNotesToEs();

    /**
     * 增量同步（更新/新增单条笔记）
     */
    void syncNoteToEs(Long noteId);

    /**
     * 批量增量同步
     */
    void batchSyncNotesToEs(List<Long> noteIds);

    /**
     * 删除 ES 中的笔记
     */
    void deleteNoteFromEs(Long noteId);
}
